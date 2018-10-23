package repast.param.wrapper;

import com.fasterxml.jackson.jr.ob.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.NonNull;
import lombok.val;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

import java.beans.PropertyChangeEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public final class ParametersWrapper {
    private static final String USE_WEB_PARAMETERS_PARAMETER_NAME = "webService_useWebParameters";

    private static final String URL_PARAMETER_NAME = "webService_url";

    private static final String POLLING_INTERVAL_PARAMETER_NAME = "webService_pollingInterval";

    private static final boolean DEFAULT_USE_WEB_PARAMETERS = false;

    private static final String DEFAULT_URL = "http://localhost:8080";

    private static final int DEFAULT_POLLING_INTERVAL = 5000;

    private static ParametersWrapper instance;

    private LocalDateTime lastPollingDone = LocalDateTime.now();

    private ParametersWrapper() {
    }

    public static ParametersWrapper getInstance() {
        if (instance == null) {
            instance = new ParametersWrapper();

            if (useWebParameters()) {
                val parameters = getRuntimeParameters();

                try {
                    val stringComposer = JSON.std.composeString().startObject();
                    for (final String parameterName : parameters.getSchema().parameterNames()) {
                        stringComposer.put(parameterName, parameters.getValueAsString(parameterName));
                    }
                    val jsonString = stringComposer.end().finish().trim();

                    Unirest.post(url() + "/initialise")
                            .header("Content-Type", "application/json")
                            .body(jsonString)
                            .asStringAsync();
                } catch (Exception ignored) {
                }

                parameters.addPropertyChangeListener(instance::processPropertyChangeEvent);
            }
        }

        return instance;
    }

    private static boolean useWebParameters() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(USE_WEB_PARAMETERS_PARAMETER_NAME) &&
                (parameters.getSchema().getDetails(USE_WEB_PARAMETERS_PARAMETER_NAME).getType().equals(Boolean.class) ||
                        parameters.getSchema().getDetails(USE_WEB_PARAMETERS_PARAMETER_NAME).getType().equals(boolean.class));

        return isValid ? parameters.getBoolean(USE_WEB_PARAMETERS_PARAMETER_NAME) : DEFAULT_USE_WEB_PARAMETERS;
    }

    private static String url() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(URL_PARAMETER_NAME) &&
                parameters.getSchema().getDetails(URL_PARAMETER_NAME).getType().equals(String.class);

        return isValid ? parameters.getString(URL_PARAMETER_NAME) : DEFAULT_URL;
    }

    private static int pollingInterval() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(POLLING_INTERVAL_PARAMETER_NAME) &&
                (parameters.getSchema().getDetails(POLLING_INTERVAL_PARAMETER_NAME).getType().equals(Integer.class) ||
                        parameters.getSchema().getDetails(POLLING_INTERVAL_PARAMETER_NAME).getType().equals(int.class));

        return isValid ? parameters.getInteger(POLLING_INTERVAL_PARAMETER_NAME) : DEFAULT_POLLING_INTERVAL;
    }

    private static Parameters getRuntimeParameters() {
        return RunEnvironment.getInstance().getParameters();
    }

    public Parameters getParameters() {
        val parameters = getRuntimeParameters();

        if (useWebParameters()) {
            if (lastPollingDone.plus(Duration.ofMillis(pollingInterval())).isAfter(LocalDateTime.now())) {
                try {
                    Unirest.get(url() + "/parameters")
                            .asStringAsync(new Callback<String>() {
                                @Override
                                public void completed(HttpResponse<String> response) {
                                    if (response.getStatus() == 200) {
                                        try {
                                            final Map<String, Object> webserviceParameters = JSON.std.mapFrom(response.getBody());

                                            webserviceParameters.forEach((name, value) -> {
                                                if (parameters.getSchema().contains(name)) {
                                                    parameters.setValue(name, value);
                                                }
                                            });
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }

                                @Override
                                public void failed(UnirestException ignored) {
                                }

                                @Override
                                public void cancelled() {
                                }
                            });
                } catch (Exception ignored) {
                }

                lastPollingDone = LocalDateTime.now();
            }
        }

        return parameters;
    }

    private void processPropertyChangeEvent(@NonNull final PropertyChangeEvent event) {
        if (useWebParameters()) {
            val parameters = getRuntimeParameters();

            val parameterExists = parameters.getSchema().contains(event.getPropertyName());
            val valueWasUpdated = !event.getNewValue().equals(event.getOldValue());

            if (parameterExists && valueWasUpdated) {
                Unirest.post(url() + "/set-parameter")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .field("name", event.getPropertyName())
                        .field("value", parameters.getValueAsString(event.getPropertyName()))
                        .asStringAsync();
            }
        }
    }
}
