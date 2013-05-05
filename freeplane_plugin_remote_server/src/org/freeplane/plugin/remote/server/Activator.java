package org.freeplane.plugin.remote.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FS;
import org.freeplane.core.util.Compat;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.jboss.netty.channel.ChannelException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class Activator implements BundleActivator{

	@Override
	public void start(BundleContext context) {
		//first thing: start logger and redirect out and err
		Logger.getLogger();
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
		
		
		final Bundle systemBundle = context.getBundle(0);
		
		logGit();
		saveAutoProperties();
		registerToFreeplaneStart(context);

		registerShutDownHook(context);

		generatePIDFile(systemBundle);
	}


	private void logGit() {
		try {
	        File gitDir = new File(".git");
	        if (gitDir.exists()){
		        Repository repo = RepositoryCache.open(RepositoryCache.FileKey.lenient(gitDir, FS.DETECTED), true);
		        Git git = new Git(repo);
		        RevCommit commit = git.log().call().iterator().next();
		        Logger.getLogger().info("Activator.logGit => Latest Commit id '" + commit.getId()+"'");
		        Logger.getLogger().info("Activator.logGit => Latest commit by '" + commit.getCommitterIdent().getName()+"'");
		        Logger.getLogger().info("Activator.logGit => Latest commit at " + new Date(commit.getCommitTime() * 1000L));
		        Logger.getLogger().info("Activator.logGit => Latest commit message '" + commit.getShortMessage()+"'");
		        repo.close();
	        } else {
	        	Logger.getLogger().info("Activator.logGit => No local git repos to log. try to find generated git file.");
	        	File gitFile = new File("./resources/gitinfo.properties");
	        	if (gitFile.exists()){
	        		BufferedReader reader = new BufferedReader(new FileReader(gitFile));
	        		Logger.getLogger().info("Activator.logGit => Latest commit info of current build " + reader.readLine());
	        	} else {
	        		Logger.getLogger().info("Activator.logGit => No commit information found.");
	        	}
	        }
		} catch (IOException ex) {
			Logger.getLogger().error("Activator.logGit => IOException", ex);
		} catch (NoHeadException e) {
			Logger.getLogger().error("Activator.logGit => NoHeadException", e);
		} catch (GitAPIException e) {
			Logger.getLogger().error("Activator.logGit => GitAPIException", e);
		}
    }

	private void registerToFreeplaneStart(final BundleContext context) {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		final Bundle systemBundle = context.getBundle(0);
		
		props.put("mode", new String[] { MModeController.MODENAME });
		context.registerService(IModeControllerExtensionProvider.class.getName(),
				new IModeControllerExtensionProvider() {
			public void installExtension(ModeController modeController) {
				try {
					RemoteController.getInstance();
				} catch(ChannelException e) {
					Logger.getLogger().error("Activator.registerToFreeplaneStart => Error while starting RemotePlugin!\n",e);
					Logger.getLogger().info("Activator.registerToFreeplaneStart => Stop Remote.");
					stop(context);
					try{systemBundle.stop();} catch(Exception e1) {}
					Logger.getLogger().info("Activator.registerToFreeplaneStart => OSGI SystemBundle stopped. Exit.");
					System.exit(1);
				}
			}
		}, props);
	}
	
	private void registerShutDownHook(BundleContext context) {
		final Bundle systemBundle = context.getBundle(0);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override 	
			public void run() {
				Logger.getLogger().info("Activator.registerShutDownHook => Shutdown hook called.");
				try {
					systemBundle.stop();
				} catch (BundleException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void saveAutoProperties() {
		//send auto.properties to correct location
		final String freeplaneDirectory = Compat.getFreeplaneUserDirectory();
		final File userPropertiesFolder = new File(freeplaneDirectory);
		final File autoPropertiesFile = new File(userPropertiesFolder, "auto.properties");
		Logger.getLogger().info("Activator.start => trying to save auto.properties to '{}'.",autoPropertiesFile.getAbsolutePath());
		InputStream in = null;
		OutputStream out = null;
		try {
			in = RemoteController.class.getResourceAsStream("/auto.properties");
			out = new FileOutputStream(autoPropertiesFile);
			IOUtils.copy(in, out);
		} catch (Exception e) {
			Logger.getLogger().error("could not save auto.properties. ",e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
	
	/**
	 * The PID-File indicates that the server is currently running.
	 * Only one server instance can run at a time.
	 * OSGI-Container will shut down if, a server is already running.
	 * 
	 */
	private void generatePIDFile(Bundle systemBundle){
		RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
		String name = rtb.getName();
		Integer pid = Integer.parseInt(name.substring(0, name.indexOf("@")));
		
		Logger.getLogger().info("OSGI pid: {}",pid);
		
		
        FileWriter fileWriter = null;
        try {
            File pidFile = new File("RUNNING_PID");
            Logger.getLogger().info("Activator.generatePIDFile => Path of RUNNING_PID = '{}'",pidFile.getAbsolutePath());
            String pidProperty = System.getProperty("ignorePID");
            if (pidFile.exists() &&  (pidProperty == null || pidProperty.isEmpty() || !pidProperty.equals("true"))){
            	Logger.getLogger().error("Activator.generatePIDFile => RUNNING_PID already exists. Abort start.");
            	systemBundle.stop();
            	Logger.getLogger().info("Activator.generatePIDFile => OSGI SystemBundle stopped. Exit.");
            	System.exit(1);
            }
            fileWriter = new FileWriter(pidFile);
            fileWriter.write(pid.toString());
            fileWriter.close();
        } catch (IOException ex){
        	System.err.println(ex.getMessage());
		} catch (BundleException e) {
			e.printStackTrace();
		} finally {
            try {
                fileWriter.close();
            } catch (IOException ex) {
            	System.err.println(ex.getMessage());
            }
        }
	}
	
	@Override
	public void stop(BundleContext context) {
		Logger.getLogger().info("Activator.stop => Activator.stop called.");
		
		if (RemoteController.isStarted()){
			Logger.getLogger().info("Activator.stop => Stop running Remote.");
			RemoteController.stop();
		}

		try{		
			Logger.getLogger().info("Activator.stop => Delete RUNNING_PID.");
    		File file = new File("./RUNNING_PID");
    		if(!file.delete()){
    			Logger.getLogger().error("Activator.stop => Error while deleting RUNNING_PID");
    		} else {
    			Logger.getLogger().info("Activator.stop => Deleted RUNNING_PID.");
    		}
    	}catch(Exception e){
    		Logger.getLogger().error("Activator.stop => Error while deleting RUNNING_PID",e);
    	}
	}
}
