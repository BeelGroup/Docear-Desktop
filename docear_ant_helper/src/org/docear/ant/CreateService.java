package org.docear.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.freeplane.ant.TaskUtils;

public class CreateService extends Task {
	private static final String DOCEAR_SERVICE_PREFIX = "docear_service_";
	private String serviceName;
	private File newServiceDir;
	private File serviceTemplateDir;
	private File baseDir;

	@Override
    public void execute() {
		baseDir = (baseDir == null) ? getProject().getBaseDir().getParentFile() : baseDir;
		readAndValidateParameters();
		newServiceDir = new File(baseDir, DOCEAR_SERVICE_PREFIX + serviceName);
		if (newServiceDir.exists())
			fatal("won't overwrite output directory " + newServiceDir + " - please remove it first");
		createDirs();
		try {
			createSources();
			createOtherFiles();
		}
		catch (IOException e) {
			throw new BuildException("error creating files: " + e.getMessage(), e);
		}
		finalWords();
	}

	private void readAndValidateParameters() {
		if (serviceName == null) {
			serviceName = TaskUtils.ask(getProject(), "=> Please enter required service name:", null);
			assertNotNull(serviceName, "property 'serviceName' is required");
		}
		serviceName = serviceName.replaceAll(DOCEAR_SERVICE_PREFIX, "").toLowerCase();
		if (!serviceName.matches("[a-z]+"))
			fatal("plugin name may only contain letters from the range [a-z]");
	}

	private void createDirs() {
		String[] subdirs = { ".settings" //
		        , "ant" //
		        , "lib" //
		        , "META-INF" //
		        , "src" //
		        , "src/org" //
		        , "src/org/freeplane" //
		        , "src/org/freeplane/plugin" //
		        , "src/org/freeplane/plugin/" + serviceName //
		};
		mkdir(newServiceDir);
		for (String dir : subdirs) {
			mkdir(new File(newServiceDir, dir));
		}
	}

	private void createSources() throws IOException {
		createService();
	}

	private void createService() throws IOException {
		String source = "package " + packageName() + ";\n" + new Scanner(getClass().getResourceAsStream("/Service.java"), "UTF-8").useDelimiter("\\A").next().replaceAll(Pattern.quote("$$$$"), TaskUtils.firstToUpper(serviceName));
		write(new File(sourceDir(), TaskUtils.firstToUpper(serviceName)+"Service.java"), source);
	}

	private void createOtherFiles() throws IOException {
		String[] files = { //
		".classpath" //
		        , ".project" //
		        , ".settings/org.eclipse.core.resources.prefs" //
		        , ".settings/org.eclipse.core.runtime.prefs" //
		        , ".settings/org.eclipse.jdt.core.prefs" //
		        , ".settings/org.eclipse.pde.core.prefs" //
		        , "ant/ant.properties" //
		        , "ant/build.xml" //
		        , "META-INF/MANIFEST.MF" //
		};
		for (String fileName : files) {
			final String content = TaskUtils.readFile(new File(serviceTemplateDir, fileName));
			final File newFile = new File(newServiceDir, fileName);
			write(newFile, transform(content));
		}
		// build.properties were missing in 1_0_x so don't try to copy them
		write(new File(newServiceDir, "build.properties"), "source.lib/plugin.jar = src/\n");
	}

	private String transform(String content) {
		return content //
		    .replaceAll("<classpathentry kind=\"lib\"[^>]*>\\s*", "") // .classpath special
		    .replaceAll("(jlatexmath.jar = )", "# $1") // ant.properties special
		    .replaceAll("lib/jlatexmath.jar,\\s*(lib/plugin.jar)", "$1") // MANIFEST.MF special
		    .replace("${commons-lang.jar}:${forms.jar}:${SimplyHTML.jar}:${jlatexmath.jar}", "") // build.xml special
		    .replaceAll("latex", serviceName) //
		    .replaceAll("Latex", TaskUtils.firstToUpper(serviceName)) //
		    .replaceAll("LATEX", serviceName.toUpperCase()) //
		;
	}

	private void write(File file, String content) throws IOException {
		Writer output = new BufferedWriter(new FileWriter(file));
		try {
			// assuming that default encoding is OK!
			output.write(content);
		}
		finally {
			output.close();
		}
	}

	private void finalWords() {
		String buildFragment = "  <antcall target=\"makeService\" inheritall=\"false\">\n" //
		        + "    <param name=\"anttarget\" value=\"dist\"/>\n" //
		        + "    <param name=\"plugindir\" value=\"docear_service_" + serviceName + "\"/>\n" //
		        + "    <param name=\"pluginname\" value=\"org.docear.service." + serviceName + "\"/>\n" //
		        + "  </antcall>\n";
		log("New service created in " + newServiceDir);
		log("What next?");
		log("* import plugin into Eclipse via Import... -> Existing Projects into Workspace");
		log("* add required external jars to " + new File(newServiceDir, "lib"));
		log("* search for \"TODO\" in the project and fill the gaps");
		log("* add the following element to docear_framework/ant/build.xml target \"build_docear\":\n" + buildFragment);
	}

	private File sourceDir() {
		return new File(newServiceDir, "src/org/docear/desktop/service/" + serviceName.toLowerCase());
	}

	private String packageName() {
		return "org.docear.desktop.service." + serviceName.toLowerCase();
	}

	private void mkdir(File dir) {
		if (!dir.mkdir())
			fatal(("cannot create directory " + dir));
	}

	private void assertNotNull(Object property, String message) {
		if (property == null)
			fatal(message);
	}

	private void fatal(String message) {
		log(message, Project.MSG_ERR);
		throw new BuildException(message);
	}

	// == properties
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String pluginName) {
		this.serviceName = pluginName;
	}

	public File getBaseDir() {
    	return baseDir;
    }

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public void setBaseDir(String baseDir) {
		setBaseDir(new File(baseDir));
	}
	

}
