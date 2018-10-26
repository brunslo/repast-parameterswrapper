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

    private static final int DEFAULT_POLLING_INTERVAL = 1000;

    private static ParametersWrapper instance;

    private LocalDateTime lastPollingDone = LocalDateTime.now();

    private ParametersWrapper() {
    }

    public static ParametersWrapper getInstance() {
        if (instance == null) {
            instance = ParametersWrapper.create();
        }

        return instance;
    }

    private static ParametersWrapper create() {
        val parametersWrapper = new ParametersWrapper();

        parametersWrapper.initialiseWebParameters();
        parametersWrapper.registerRuntimeParametersChangeListener();

        return parametersWrapper;
    }

    public Parameters getParameters() {
        updateRuntimeParameters();

        return getRuntimeParameters();
    }

    private void initialiseWebParameters() {
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
                        .asStringAsync(new Callback<String>() {
                            @Override
                            public void completed(HttpResponse<String> response) {
                                if (response.getStatus() != 200) {
                                    log("Call to /initialise did not return 200: " + response);
                                }
                            }

                            @Override
                            public void failed(UnirestException ex) {
                                log("Call to /initialise failed: " + ex.getLocalizedMessage());
                            }

                            @Override
                            public void cancelled() {
                                log("Call to /initialise cancelled");
                            }
                        });
            } catch (Exception ex) {
                log("Call to /initialise raised unexpected exception: " + ex.getLocalizedMessage());
            }
        }
    }

    private void registerRuntimeParametersChangeListener() {
        if (useWebParameters()) {
            val parameters = getRuntimeParameters();

            parameters.addPropertyChangeListener(this::processPropertyChangeEvent);
        }
    }

    private void updateRuntimeParameters() {
        if (useWebParameters()) {
            val nextPollingDue = lastPollingDone.plus(Duration.ofMillis(pollingInterval()));

            if (LocalDateTime.now().isAfter(nextPollingDue)) {
                try {
                    val parameters = getRuntimeParameters();

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
                                        } catch (Exception ex) {
                                            log("Error parsing /parameters response: " + ex.getLocalizedMessage());
                                        }
                                    } else {
                                        log("Call to /parameters did not return 200: " + response);
                                    }
                                }

                                @Override
                                public void failed(UnirestException ex) {
                                    log("Call to /parameters failed: " + ex.getLocalizedMessage());
                                }

                                @Override
                                public void cancelled() {
                                    log("Call to /parameters cancelled");
                                }
                            });
                } catch (Exception ex) {
                    log("Call to /parameters raised unexpected exception: " + ex.getLocalizedMessage());
                }

                lastPollingDone = LocalDateTime.now();
            }
        }
    }

    private void processPropertyChangeEvent(@NonNull final PropertyChangeEvent event) {
        if (useWebParameters()) {
            val parameters = getRuntimeParameters();

            val parameterExists = parameters.getSchema().contains(event.getPropertyName());
            val valueWasUpdated = !event.getNewValue().equals(event.getOldValue());

            if (parameterExists && valueWasUpdated) {
                val name = event.getPropertyName();
                val value = parameters.getValueAsString(event.getPropertyName());

                Unirest.post(url() + "/set-parameter")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .field("name", name)
                        .field("value", value)
                        .asStringAsync(new Callback<String>() {
                            @Override
                            public void completed(HttpResponse<String> response) {
                                if (response.getStatus() != 200) {
                                    log(String.format(
                                            "Call to /set-parameter with name='%s' and value='%s' did not return 200: %s",
                                            name, value, response
                                    ));
                                }
                            }

                            @Override
                            public void failed(UnirestException ex) {
                                log("Call to /set-parameter failed: " + ex.getLocalizedMessage());
                            }

                            @Override
                            public void cancelled() {
                                log("Call to /set-parameter cancelled");
                            }
                        });
            }
        }
    }

    private boolean useWebParameters() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(USE_WEB_PARAMETERS_PARAMETER_NAME) &&
                (parameters.getSchema().getDetails(USE_WEB_PARAMETERS_PARAMETER_NAME).getType().equals(Boolean.class) ||
                        parameters.getSchema().getDetails(USE_WEB_PARAMETERS_PARAMETER_NAME).getType().equals(boolean.class));

        return isValid ? parameters.getBoolean(USE_WEB_PARAMETERS_PARAMETER_NAME) : DEFAULT_USE_WEB_PARAMETERS;
    }

    private String url() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(URL_PARAMETER_NAME) &&
                parameters.getSchema().getDetails(URL_PARAMETER_NAME).getType().equals(String.class);

        return isValid ? parameters.getString(URL_PARAMETER_NAME) : DEFAULT_URL;
    }

    private int pollingInterval() {
        val parameters = getRuntimeParameters();

        val isValid = parameters.getSchema().contains(POLLING_INTERVAL_PARAMETER_NAME) &&
                (parameters.getSchema().getDetails(POLLING_INTERVAL_PARAMETER_NAME).getType().equals(Integer.class) ||
                        parameters.getSchema().getDetails(POLLING_INTERVAL_PARAMETER_NAME).getType().equals(int.class));

        return isValid ? parameters.getInteger(POLLING_INTERVAL_PARAMETER_NAME) : DEFAULT_POLLING_INTERVAL;
    }

    private Parameters getRuntimeParameters() {
        return RunEnvironment.getInstance().getParameters();
    }

    private void log(@NonNull final String message) {
        System.out.println(String.format("%s: %s",
                ParametersWrapper.class.getSimpleName(), message
        ));
    }
}
