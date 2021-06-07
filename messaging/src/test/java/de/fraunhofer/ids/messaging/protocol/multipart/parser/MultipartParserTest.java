package de.fraunhofer.ids.messaging.protocol.multipart.parser;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okio.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class MultipartParserTest {

    @Test
    void testParseMultipart() throws Exception {
        //create a MultipartString for parsing
        final var multipart = new MultipartBody.Builder().addFormDataPart("header", "value1").addFormDataPart("payload", "value2").build();
        final Buffer buffer = new Buffer();

        multipart.writeTo(buffer);
        final var multipartString = buffer.readUtf8();

        if (log.isInfoEnabled()) {
            log.info(multipartString);
        }

        //parse the string and check if header and payload were parsed correctly
        final var map = MultipartParser.stringToMultipart(multipartString);

        assertEquals("value1", map.get("header"));
        assertEquals("value2", map.get("payload"));
        //parse a string which is not multipart, should throw exception
        assertThrows(MultipartParseException.class, () -> MultipartParser.stringToMultipart("This is not Multipart."));
        //should find a boundary, but no multipart parts, should throw exception, too
        assertThrows(MultipartParseException.class, () -> MultipartParser.stringToMultipart("This is not Multipart.\nBut it has a linebreak."));
    }

}
