package repast.param.wrapper;

import com.fasterxml.jackson.jr.ob.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.NonNull;
import lombok.val;
import repast.param.wrapper.util.LoggingUtils;
import repast.param.wrapper.util.RuntimeParametersUtils;
import repast.simphony.parameter.Parameters;

import java.beans.PropertyChangeEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ParametersWrapper {
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

        parametersWrapper.initialise();
        parametersWrapper.registerRuntimeParametersChangeListener();

        return parametersWrapper;
    }

    public Parameters getParameters() {
        updateRuntimeParameters();

        return RuntimeParametersUtils.getRuntimeParameters();
    }

    public void initialise() {
        if (RuntimeParametersUtils.useWebParameters()) {
            Unirest.post(RuntimeParametersUtils.url() + "/parameters/initialise")
                    .header("Content-Type", "application/json")
                    .asStringAsync(new Callback<String>() {
                        @Override
                        public void completed(HttpResponse<String> response) {
                            if (response.getStatus() != 200) {
                                log("Call to POST /parameters/initialise did not return 200:\n" + LoggingUtils.printResponse(response));
                            } else {
                                publishWebParameters();
                            }
                        }

                        @Override
                        public void failed(UnirestException ex) {
                            log("Call to POST /parameters/initialise failed: " + ex.getLocalizedMessage());
                        }

                        @Override
                        public void cancelled() {
                            log("Call to POST /parameters/initialise cancelled");
                        }
                    });
        }
    }

    private void publishWebParameters() {
        if (RuntimeParametersUtils.useWebParameters()) {
            val parameters = RuntimeParametersUtils.getRuntimeParameters();

            try {
                val parametersMap = StreamSupport.stream(parameters.getSchema().parameterNames().spliterator(), false)
                        .collect(Collectors.toMap(Function.identity(), parameters::getValueAsString));

                Unirest.post(RuntimeParametersUtils.url() + "/parameters")
                        .header("Content-Type", "application/json")
                        .body(JSON.std.asString(parametersMap))
                        .asStringAsync(new Callback<String>() {
                            @Override
                            public void completed(HttpResponse<String> response) {
                                if (response.getStatus() != 200) {
                                    log("Call to POST /parameters did not return 200:\n" + LoggingUtils.printResponse(response));
                                }
                            }

                            @Override
                            public void failed(UnirestException ex) {
                                log("Call to POST /parameters failed: " + ex.getLocalizedMessage());
                            }

                            @Override
                            public void cancelled() {
                                log("Call to POST /parameters cancelled");
                            }
                        });
            } catch (Exception ex) {
                log("Call to POST /parameters raised unexpected exception: " + ex.getLocalizedMessage());
            }
        }
    }

    private void registerRuntimeParametersChangeListener() {
        if (RuntimeParametersUtils.useWebParameters()) {
            val parameters = RuntimeParametersUtils.getRuntimeParameters();

            parameters.addPropertyChangeListener(this::processPropertyChangeEvent);
        }
    }

    private void updateRuntimeParameters() {
        if (RuntimeParametersUtils.useWebParameters()) {
            val nextPollingDue = lastPollingDone.plus(Duration.ofMillis(RuntimeParametersUtils.pollingInterval()));

            if (LocalDateTime.now().isAfter(nextPollingDue)) {
                try {
                    val parameters = RuntimeParametersUtils.getRuntimeParameters();

                    Unirest.get(RuntimeParametersUtils.url() + "/parameters")
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
                                            log("Error parsing GET /parameters response: " + ex.getLocalizedMessage());
                                        }
                                    } else {
                                        log("Call to GET /parameters did not return 200:\n" + LoggingUtils.printResponse(response));
                                    }
                                }

                                @Override
                                public void failed(UnirestException ex) {
                                    log("Call to GET /parameters failed: " + ex.getLocalizedMessage());
                                }

                                @Override
                                public void cancelled() {
                                    log("Call to GET /parameters cancelled");
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
        if (RuntimeParametersUtils.useWebParameters()) {
            val parameters = RuntimeParametersUtils.getRuntimeParameters();

            val parameterExists = parameters.getSchema().contains(event.getPropertyName());
            val valueWasUpdated = !event.getNewValue().equals(event.getOldValue());

            if (parameterExists && valueWasUpdated) {
                val name = event.getPropertyName();
                val value = parameters.getValueAsString(event.getPropertyName());

                Unirest.put(RuntimeParametersUtils.url() + "/parameters")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .field("name", name)
                        .field("value", value)
                        .asStringAsync(new Callback<String>() {
                            @Override
                            public void completed(HttpResponse<String> response) {
                                if (response.getStatus() != 200) {
                                    log(String.format(
                                            "Call to PUT /parameters with name='%s' and value='%s' did not return 200:\n%s",
                                            name, value, LoggingUtils.printResponse(response)
                                    ));
                                }
                            }

                            @Override
                            public void failed(UnirestException ex) {
                                log("Call to PUT /parameters failed: " + ex.getLocalizedMessage());
                            }

                            @Override
                            public void cancelled() {
                                log("Call to PUT /parameters cancelled");
                            }
                        });
            }
        }
    }

    private void log(@NonNull final String message) {
        LoggingUtils.log(ParametersWrapper.class, message);
    }
}
