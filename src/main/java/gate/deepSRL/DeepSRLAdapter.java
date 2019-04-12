package gate.deepSRL;

import static gate.util.Files.fileFromURL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import deepSRL.DeepSRLProcessBuilder;
import gate.Factory;
import gate.Factory.DuplicationContext;
import gate.Gate;
import gate.Resource;
import gate.creole.AbstractResource;
import gate.creole.CustomDuplication;
import gate.creole.ResourceData;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;

/**
 * This class is the implementation of the resource DeepSRLAdapter.
 */
@CreoleResource(name = "DeepSRLAdapter", comment = "Integrate DeepSRL (https://github.com/luheng/deep_srl) as a Processing Resource via Commandline Process")
public class DeepSRLAdapter extends DeepSRLAnalyser implements CustomDuplication {
	private static final long serialVersionUID = -4219643446126996233L;
	private static Logger logger = Logger.getLogger(DeepSRLAdapter.class);

	private String environment;
	private URL pythonExecutable;
	private Boolean printOutput;
	private Boolean printError;

	private URL deepSRLScript;
	private URL modelPath;
	private URL propidModelPath;

	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			DeepSRLProcessBuilder builder = new DeepSRLProcessBuilder(fileFromURL(pythonExecutable),
					fileFromURL(deepSRLScript), fileFromURL(modelPath), fileFromURL(propidModelPath));
			builder.withEnvironment(parseEnvironmentString(this.environment));
			builder.withOutputStream(printOutput ? System.out : null);
			builder.withErrorStream(printError ? System.err : null);
			initDeepSRL(builder.build());
		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
		}
		return this;
	}

	@Override
	public Resource duplicate(DuplicationContext ctx) throws ResourceInstantiationException {
		ResourceData resourceData = Gate.getCreoleRegister().get(DeepSRLAdapter.class.getCanonicalName());
		DeepSRLAdapter dulicate = new DeepSRLAdapter();

		dulicate.setName(resourceData.getName() + "_" + Gate.genSym());
		AbstractResource.setParameterValues(dulicate, getInitParameterValues());
		AbstractResource.setParameterValues(dulicate, getRuntimeParameterValues());
		dulicate.setFeatures(Factory.newFeatureMap());
		dulicate.getFeatures().putAll(getFeatures());

		dulicate.deepSRL = deepSRL;

		resourceData.addInstantiation(dulicate);
		return dulicate;
	}

	private static Map<String, String> parseEnvironmentString(String string) {
		Map<String, String> env = new LinkedHashMap<String, String>();

		String pattern = "\\b([^\\s]+)=([^\\s]+)\\b";

		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(string);

		while (m.find()) {
			env.put(m.group(1), m.group(2));
		}

		return env;
	}

	@Optional
	@CreoleParameter(comment = "environment variables (linux-style, e.g. 'MKL_THREADING_LAYER=GNU')", defaultValue = "")
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getEnvironment() {
		return environment;
	}

	@CreoleParameter(comment = "python executable, version 2.x.x required", defaultValue = "")
	public void setPythonExecutable(URL pythonExecutable) {
		this.pythonExecutable = pythonExecutable;
	}

	public URL getPythonExecutable() throws MalformedURLException {
		return pythonExecutable;
	}

	@CreoleParameter(comment = "print startup output of DeepSRL Script to Java System.out", defaultValue = "true")
	public void setPrintOutput(Boolean printOutput) {
		this.printOutput = printOutput;
	}

	public Boolean getPrintOutput() {
		return printOutput;
	}

	@CreoleParameter(comment = "print error output of DeepSRL Script to Java System.err", defaultValue = "true")
	public void setPrintError(Boolean printError) {
		this.printError = printError;
	}

	public Boolean getPrintError() {
		return printError;
	}

	@CreoleParameter(comment = "DeepSRL Script, name: \"gate_deepSRL.py\"", defaultValue = "")
	public void setDeepSRLScript(URL deepSRLScript) {
		this.deepSRLScript = deepSRLScript;
	}

	public URL getDeepSRLScript() {
		return deepSRLScript;
	}

	@CreoleParameter(comment = "DeepSRL ModelPath, name: \\conll05_model", defaultValue = "")
	public void setModelPath(URL PATH) {
		this.modelPath = PATH;
	}

	public URL getModelPath() {
		return modelPath;
	}

	@CreoleParameter(comment = "DeepSRL PropidModelPath, name: \\conll05_propid_model", defaultValue = "")
	public void setPropidModelPath(URL PATH) {
		this.propidModelPath = PATH;
	}

	public URL getPropidModelPath() {
		return propidModelPath;
	}

}
