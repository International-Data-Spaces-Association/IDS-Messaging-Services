package de.fraunhofer.ids.framework.messaging.util;

import java.io.IOException;
import java.util.Objects;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okio.Buffer;

/**
 * The MessageUtils class contains utility methods for printing and logging okhttp requests.
 */

@Slf4j
@UtilityClass
public class RequestUtils {
    /**
     * A util class that prints okhttp Requests.
     *
     * @param request a okhttp Request
     * @return the request as string or an empty string
     */
    public static String printRequest(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();

            Objects.requireNonNull(copy.body()).writeTo(buffer);

            return buffer.readUtf8();
        } catch ( IOException | NullPointerException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getClass().toString() + ": printing failed.");
            }
        }
        return "";
    }

    /**
     * Prints the request as info in the log.
     *
     * @param request {@link Request} to be logged
     */
    public static void logRequest(final Request request) {
        if (log.isInfoEnabled()) {
            log.info(printRequest(request));
        }
    }
}
