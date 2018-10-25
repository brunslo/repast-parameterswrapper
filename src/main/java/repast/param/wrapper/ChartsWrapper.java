package repast.param.wrapper;

import com.fasterxml.jackson.jr.ob.JSON;
import com.mashape.unirest.http.Unirest;
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
            instance = new ChartsWrapper();
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

                    Unirest.post(url() + "/sendCharts")
                            .header("Content-Type", "application/json")
                            .body(jsonString)
                            .asStringAsync();
                } catch(Exception ignored){
                }
        }
    }



}
