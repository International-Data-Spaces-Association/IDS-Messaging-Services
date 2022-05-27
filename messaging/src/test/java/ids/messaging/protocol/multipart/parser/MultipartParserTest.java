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
package ids.messaging.protocol.multipart.parser;

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
            log.info(multipartString + " [code=(IMSMEI0070)]");
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
