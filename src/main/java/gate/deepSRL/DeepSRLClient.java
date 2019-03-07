package gate.deepSRL;

import deepSRL.DeepSRLClientBuilder;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

/**
 * This class is the implementation of the resource DeepSRLClient.
 */
@CreoleResource(name = "DeepSRLClient", comment = "Integrate DeepSRL (https://github.com/luheng/deep_srl) as a Processing Resource via TCP Client")
public class DeepSRLClient extends DeepSRLAnalyser {
	private static final long serialVersionUID = -4905631708449534282L;

	private String host;
	private Integer port;

	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			DeepSRLClientBuilder builder = new DeepSRLClientBuilder(host, port);
			initDeepSRL(builder.build());
		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
		}
		return this;
	}

	@Override
	public void reInit() throws ResourceInstantiationException {
		init();
	}

	@CreoleParameter(comment = "Input annotation set name", defaultValue = "localhost")
	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	@CreoleParameter(comment = "Input annotation set name", defaultValue = "6756")
	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getPort() {
		return port;
	}

}
