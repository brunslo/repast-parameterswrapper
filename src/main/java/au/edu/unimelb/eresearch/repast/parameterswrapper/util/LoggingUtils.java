package au.edu.unimelb.eresearch.repast.parameterswrapper.util;

import com.mashape.unirest.http.HttpResponse;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LoggingUtils {
    public String printResponse(@NonNull final HttpResponse<?> response) {
        return String.format(" - Status: %d\n - Body: %s", response.getStatus(), response.getBody());
    }

    public void log(@NonNull final Class<?> clazz, @NonNull final String message) {
        System.out.println(String.format("%s: %s", clazz.getSimpleName(), message));
    }
}
