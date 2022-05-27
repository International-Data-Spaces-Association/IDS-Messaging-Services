/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.util;

import java.io.IOException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okio.Buffer;

/**
 * The MessageUtils class contains utility methods for printing and logging okhttp requests.
 */

@Slf4j
public final class RequestUtils {
    private RequestUtils() {
        //Nothing to do here.
    }

    /**
     * A util class that prints okhttp Requests.
     *
     * @param request A okhttp Request.
     * @return The request as string or an empty string.
     */
    public static String printRequest(final Request request) {
        var bufferText = "";
        try {
            final var copy = request.newBuilder().build();
            final var buffer = new Buffer();

            Objects.requireNonNull(copy.body()).writeTo(buffer);

            bufferText = buffer.readUtf8();
            buffer.close();
        } catch (IOException | NullPointerException e) {
            if (log.isErrorEnabled()) {
                log.error("Printing failed! [code=(IMSMEE0027), error=({})]",
                          e.getClass().toString());
            }
        }

        return bufferText;
    }

    /**
     * Prints the request as info in the log.
     *
     * @param request {@link Request} to be logged.
     */
    public static void logRequest(final Request request) {
        if (log.isInfoEnabled()) {
            log.info(printRequest(request) + " [code=(IMSMEI0069)]");
        }
    }
}
