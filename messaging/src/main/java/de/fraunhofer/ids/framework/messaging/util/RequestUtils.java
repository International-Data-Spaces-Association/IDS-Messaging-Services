package de.fraunhofer.ids.framework.messaging.util;

import java.io.IOException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okio.Buffer;

/**
 * The MessageUtils class contains utility methods for printing and logging okhttp requests.
 */

@Slf4j
public class RequestUtils {
    /**
     * A util class that prints okhttp Requests.
     *
     * @param request a okhttp Request
     *
     * @return the request as string or an empty string
     */
    public static String printRequest( Request request ) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            Objects.requireNonNull(copy.body()).writeTo(buffer);
            return ( buffer.readUtf8() );
        } catch( final IOException | NullPointerException e ) {
            log.error(e.getClass().toString()+": printing failed.");
        }
        return "";
    }

    /**
     * Prints the request as info in the log.
     *
     * @param request {@link Request} to be logged
     */
    public static void logRequest( Request request ) {
        log.info(printRequest(request));
    }
}
