package de.fraunhofer.ids.framework.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okio.Buffer;
import org.apache.commons.fileupload.FileUploadException;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MultipartParserTest {

    @Test
    public void testParseMultipart() throws Exception {
        var multipart = new MultipartBody.Builder().addFormDataPart("header", "value1").addFormDataPart("payload", "value2").build();
        final Buffer buffer = new Buffer();
        multipart.writeTo(buffer);
        var multipartString = buffer.readUtf8();
        log.info(multipartString);
        var map = MultipartParser.stringToMultipart(multipartString);
        assertEquals(map.get("header"), "value1");
        assertEquals(map.get("payload"), "value2");
    }

}