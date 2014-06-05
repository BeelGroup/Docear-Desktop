package org.docear.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;
import org.apache.tools.ant.util.StringUtils;

public class CreateService extends Task {
	private static final String DOCEAR_SERVICE_PREFIX = "docear_service_";
	private String serviceName;
	private File newServiceDir;
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
			copyTemplateFiles();
		}
		catch (IOException e) {
			throw new BuildException("error creating files: " + e.getMessage(), e);
		}
		finalWords();
	}

	private void readAndValidateParameters() {
		if (serviceName == null) {
			serviceName = ask(getProject(), "=> Please enter required service name:", null);
			assertNotNull(serviceName, "property 'serviceName' is required");
		}
		serviceName = serviceName.replaceAll(DOCEAR_SERVICE_PREFIX, "").toLowerCase();
		if (!serviceName.matches("[a-z]+")) {
			fatal("plugin name may only contain letters from the range [a-z]");
		}
	}

	private void createDirs() {
		String[] subdirs = { ".settings" //
		        , "lib" //
		        , "resources" //
		        , "META-INF" //
		        , "META-INF/services" //
		        , "src" //
		        , "src/org" //
		        , "src/org/docear" //
		        , "src/org/docear/desktop" //
		        , "src/org/docear/desktop/service" //
		        , "src/org/docear/desktop/service/" + serviceName //
		};
		log("create docear service dir: "+ newServiceDir);
		mkdir(newServiceDir);
		for (String dir : subdirs) {
			mkdir(new File(newServiceDir, dir));
		}
	}

	private void copyTemplateFiles() throws IOException {
		String[] files = { //
				".classpath" //
		        , ".project" //
		        , ".settings/org.eclipse.jdt.core.prefs" //
		        , "build.properties" //
		        , "build.xml" //
		        , "META-INF/org.docear.services.desc" //
		        , "META-INF/services/org.freeplane.plugin.docear.core.spi.DocearService" //
		        , "src/org/docear/desktop/service/servicename/ServiceNameService.java" //
		};
		for (String fileName : files) {
			String template = "/template/" + fileName;
			final String content = readFile(getClass().getResourceAsStream(template), "UTF-8"/*"US-ASCII"*/);
			final File newFile = new File(newServiceDir, transform(fileName));
			log("creating file: " + newFile);
			write(newFile, transform(content));
		}
	}

	private String transform(String content) {
		return content //
		    .replaceAll("servicename", serviceName) //
		    .replaceAll("ServiceName", firstToUpper(serviceName)) //
		    .replaceAll("SERVICE_NAME", serviceName.toUpperCase()) //
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
		        + "    <param name=\"servicedir\" value=\"docear_service_" + serviceName + "\"/>\n" //
		        + "  </antcall>\n";
		log("\n\nNew service created in " + newServiceDir);
		log("\nWhat next?");
		log("* import plugin into Eclipse via Import... -> Existing Projects into Workspace");
		log("* add required external jars to " + new File(newServiceDir, "lib"));
		log("* add the following element to docear_framework/ant/build.xml -> target \"build_services\":\n" + buildFragment);
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
	
	public static String readFile(final InputStream input, String encoding) throws IOException {
		if(input == null) {
			throw new IOException("null input passed to CreateService.readFile()");
		}
		InputStreamReader in = null;
		try {
			in = new InputStreamReader(input, encoding);
			StringBuilder builder = new StringBuilder();
			final char[] buf = new char[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				builder.append(buf, 0, len);
			}
			return builder.toString();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					// can't help it
				}
			}
		}
	}
	
	public static String readFile(final File inputFile, String encoding) throws IOException {
		return readFile(new FileInputStream(inputFile), encoding);
	}

	

	String firstToUpper(String string) {
    	if (string == null || string.length() < 2)
    		return string;
    	return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

	@SuppressWarnings("unchecked")
    String multipleChoice(Project project, String message, String validValues, String defaultValue) {
    	InputRequest request = null;
    	if (validValues != null) {
    		Vector<String> accept = StringUtils.split(validValues, ',');
    		request = new MultipleChoiceInputRequest(message, accept);
    	}
    	else {
    		request = new InputRequest(message);
    	}
    	InputHandler handler = project.getInputHandler();
    	handler.handleInput(request);
    	final String value = request.getInput();
    	if ((value == null || value.trim().length() == 0) && defaultValue != null) {
    		return defaultValue;
    	}
    	return value;
    }

	String ask(Project project, String message, String defaultValue) {
    	return multipleChoice(project, message, defaultValue, null);
    }
	

}
