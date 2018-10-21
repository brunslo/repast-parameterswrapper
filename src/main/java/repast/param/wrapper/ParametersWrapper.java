package repast.param.wrapper;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

import java.io.IOException;

public final class ParametersWrapper {
	private static boolean USE_WEBCLIENT;

	private static ParametersWrapper instance;
	private static  String ENDPOINT_URL;

	private ParametersWrapper() {
	}

	public static ParametersWrapper getInstance() {
		if (instance == null) {
			instance = new ParametersWrapper();
		}

		return instance;
	}

	public void setEndPointURL(String _url) {
		ENDPOINT_URL= _url;
	}

	public void setWebClient(Boolean input) {
		USE_WEBCLIENT=input;
	}
	public Parameters getParameters() throws IOException {
		return USE_WEBCLIENT ? WebclientParameters.receiveParam(ENDPOINT_URL) : RunEnvironment.getInstance().getParameters();
	}

	public void setParameters() throws IOException {
		if(USE_WEBCLIENT)
		{
			WebclientParameters.transmitParam(ENDPOINT_URL);
		}
	}
}
