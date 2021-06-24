package de.fraunhofer.ids.messaging.util;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RequestUtilsTest {
    @Test
    public void testRequestUtils() {
        final var req = new Request.Builder()
                .post(RequestBody.create("String", MediaType.parse("text/plain")))
                .url("http://example.com")
                .build();

        assertDoesNotThrow(() -> RequestUtils.logRequest(req));
        assertDoesNotThrow(() -> RequestUtils.printRequest(req));
    }
}
