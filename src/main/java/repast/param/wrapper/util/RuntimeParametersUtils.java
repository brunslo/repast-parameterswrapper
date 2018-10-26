package repast.param.wrapper.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuntimeParametersUtils {
    private static final String USE_WEB_PARAMETERS_PARAMETER_NAME = "webService_useWebParameters";

    private static final String URL_PARAMETER_NAME = "webService_url";

    private static final String POLLING_INTERVAL_PARAMETER_NAME = "webService_pollingInterval";

    private static final boolean DEFAULT_USE_WEB_PARAMETERS = false;

    private static final String DEFAULT_URL = "http://localhost:8080";

    private static final int DEFAULT_POLLING_INTERVAL = 1000;

    public static boolean useWebParameters() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(USE_WEB_PARAMETERS_PARAMETER_NAME) &&
                (parameters.getSchema().getDetails(USE_WEB_PARAMETERS_PARAMETER_NAME).getType().equals(Boolean.class) ||
                        parameters.getSchema().getDetails(USE_WEB_PARAMETERS_PARAMETER_NAME).getType().equals(boolean.class));

        return isValid ? parameters.getBoolean(USE_WEB_PARAMETERS_PARAMETER_NAME) : DEFAULT_USE_WEB_PARAMETERS;
    }

    public static String url() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(URL_PARAMETER_NAME) &&
                parameters.getSchema().getDetails(URL_PARAMETER_NAME).getType().equals(String.class);

        return isValid ? parameters.getString(URL_PARAMETER_NAME) : DEFAULT_URL;
    }

    public static int pollingInterval() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(POLLING_INTERVAL_PARAMETER_NAME) &&
                (parameters.getSchema().getDetails(POLLING_INTERVAL_PARAMETER_NAME).getType().equals(Integer.class) ||
                        parameters.getSchema().getDetails(POLLING_INTERVAL_PARAMETER_NAME).getType().equals(int.class));

        return isValid ? parameters.getInteger(POLLING_INTERVAL_PARAMETER_NAME) : DEFAULT_POLLING_INTERVAL;
    }

    public static Parameters getRuntimeParameters() {
        return RunEnvironment.getInstance().getParameters();
    }
}
