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
import java.util.Iterator;
import java.util.Map;

public class ChartsWrapper {
    private static final String USE_WEB_PARAMETERS_PARAMETER_NAME = "webService_useWebParameters";

    private static final String URL_PARAMETER_NAME = "webService_url";

    private static final boolean DEFAULT_USE_WEB_PARAMETERS = false;

    private static final String DEFAULT_URL = "http://localhost:8080";

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
        val ChartsWrapper = new ChartsWrapper();
        ChartsWrapper.initialiseWebCharts();
        return ChartsWrapper;
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

    private static Parameters getRuntimeParameters() {

        return RunEnvironment.getInstance().getParameters();
    }

    public void sendChartData(Map<String,String> chartMap) {

        if(useWebParameters())
        {
            try {
                    val stringComposer = JSON.std.composeString().startObject();
                    Iterator it = chartMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        stringComposer.put(pair.getKey().toString(), pair.getValue().toString());
                        it.remove();
                    }

                    val jsonString = stringComposer.end().finish().trim();

                    Unirest.post(url() + "/charts")
                            .header("Content-Type", "application/json")
                            .body(jsonString)
                            .asStringAsync(new Callback<String>() {
                                @Override
                                public void completed(HttpResponse<String> response) {
                                    if (response.getStatus() != 200) {
                                        log("Call to /charts did not return 200: " + response);
                                    }
                                }

                                @Override
                                public void failed(UnirestException ex) {
                                    log("Call to /charts failed: " + ex.getLocalizedMessage());
                                }

                                @Override
                                public void cancelled() {
                                    log("Call to /charts cancelled");
                                }
                            });

                } catch(Exception ignored){
                }
        }
    }


    public void initialiseWebCharts() {
        if (useWebParameters()) {

            try {
//                val stringComposer = JSON.std.composeString().startObject();
//                val jsonString = stringComposer.end().finish().trim();

                Unirest.post(url() + "/initialise-chart")
                        .header("Content-Type", "application/json")
//                        .body(jsonString)
                        .asStringAsync(new Callback<String>() {
                            @Override
                            public void completed(HttpResponse<String> response) {
                                if (response.getStatus() != 200) {
                                    log("Call to /initialise-chart chart did not return 200: " + response);
                                }
                            }

                            @Override
                            public void failed(UnirestException ex) {
                                log("Call to /initialise-chart chart failed: " + ex.getLocalizedMessage());
                            }

                            @Override
                            public void cancelled() {
                                log("Call to /initialise-chart chart cancelled");
                            }
                        });
            } catch (Exception ex) {
                log("Call to /initialise-chart raised unexpected exception: " + ex.getLocalizedMessage());
            }
        }
    }

    private void log(@NonNull final String message) {
        System.out.println(String.format("%s: %s",
                ParametersWrapper.class.getSimpleName(), message
        ));
    }


}
