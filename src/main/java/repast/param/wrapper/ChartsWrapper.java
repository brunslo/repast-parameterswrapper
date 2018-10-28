package repast.param.wrapper;

import com.fasterxml.jackson.jr.ob.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.NonNull;
import lombok.val;

import java.util.List;
import java.util.Map;

import static repast.param.wrapper.util.RuntimeParametersUtils.url;
import static repast.param.wrapper.util.RuntimeParametersUtils.useWebParameters;

public class ChartsWrapper {
    private static ChartsWrapper instance;

    private ChartsWrapper() {
    }

    public static ChartsWrapper getInstance() {
        if (instance == null) {
            instance = ChartsWrapper.create();
        }

        return instance;
    }

    private static ChartsWrapper create() {
        val chartsWrapper = new ChartsWrapper();

        chartsWrapper.initialise();

        return chartsWrapper;
    }

    public void initialise() {
        if (useWebParameters()) {
            Unirest.post(url() + "/charts/initialise")
                    .header("Content-Type", "application/json")
                    .asStringAsync(new Callback<String>() {
                        @Override
                        public void completed(HttpResponse<String> response) {
                            if (response.getStatus() != 200) {
                                log("Call to POST /charts/initialise did not return 200: " + response);
                            }
                        }

                        @Override
                        public void failed(UnirestException ex) {
                            log("Call to POST /charts/initialise failed: " + ex.getLocalizedMessage());
                        }

                        @Override
                        public void cancelled() {
                            log("Call to POST /charts/initialise cancelled");
                        }
                    });
        }
    }

    public void publishCharts(@NonNull final List<Map<String, String>> charts) {
        if (useWebParameters()) {
            try {
                Unirest.post(url() + "/charts")
                        .header("Content-Type", "application/json")
                        .body(JSON.std.asString(charts))
                        .asStringAsync(new Callback<String>() {
                            @Override
                            public void completed(HttpResponse<String> response) {
                                if (response.getStatus() != 200) {
                                    log("Call to POST /charts did not return 200: " + response);
                                }
                            }

                            @Override
                            public void failed(UnirestException ex) {
                                log("Call to POST /charts failed: " + ex.getLocalizedMessage());
                            }

                            @Override
                            public void cancelled() {
                                log("Call to POST /charts cancelled");
                            }
                        });
            } catch (Exception ex) {
                log("Call to POST /charts raised unexpected exception: " + ex.getLocalizedMessage());
            }
        }
    }

    public void publishSingleChartMap(Map<String, String> chartMap) {
        if (useWebParameters()) {
            try {
                Unirest.put(url() + "/charts")
                        .header("Content-Type", "application/json")
                        .body(JSON.std.asString(chartMap))
                        .asStringAsync(new Callback<String>() {
                            @Override
                            public void completed(HttpResponse<String> response) {
                                if (response.getStatus() != 200) {
                                    log("Call to PUT /charts did not return 200: " + response);
                                }
                            }

                            @Override
                            public void failed(UnirestException ex) {
                                log("Call to PUT /charts failed: " + ex.getLocalizedMessage());
                            }

                            @Override
                            public void cancelled() {
                                log("Call to PUT /charts cancelled");
                            }
                        });
            } catch (Exception ex) {
                log("Call to PUT /charts raised unexpected exception: " + ex.getLocalizedMessage());
            }
        }
    }

    private void log(@NonNull final String message) {
        System.out.println(String.format("%s: %s", ChartsWrapper.class.getSimpleName(), message));
    }
}
