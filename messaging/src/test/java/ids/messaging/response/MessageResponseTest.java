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
package ids.messaging.response;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.RequestMessageBuilder;
import de.fraunhofer.iais.eis.ResponseMessage;
import de.fraunhofer.iais.eis.ResponseMessageBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import ids.messaging.common.SerializeException;
import ids.messaging.util.IdsMessageUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageResponseTest {

    @Test
    void testResponses() throws IOException, SerializeException {
        final var empty = MessageResponse.empty();
        assertNotNull(empty.createMultipartMap(new Serializer()));
        final var bodyRes = BodyResponse.create(buildResponseMessage(), "String payload!");
        assertNotNull(bodyRes.createMultipartMap(new Serializer()));
        assertThrows(IllegalStateException.class, () -> BodyResponse.create(buildRequestMessage(), "String paylaod!"));
        final var errRes =  ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS,
                "request",
                URI.create("http://uri"),
                "4.0");
        assertNotNull(errRes.createMultipartMap(new Serializer()));
        final var fileRes = Base64EncodedFileBodyResponse.create(buildResponseMessage(), File.createTempFile("temp","file"), MediaType.IMAGE_JPEG);
        assertNotNull(fileRes.createMultipartMap(new Serializer()));
        assertThrows(IllegalStateException.class, () -> Base64EncodedFileBodyResponse.create(buildRequestMessage(), File.createTempFile("temp","file"), MediaType.IMAGE_JPEG));
    }

    private RequestMessage buildRequestMessage() {
        final var now = IdsMessageUtils.getGregorianNow();
        return new RequestMessageBuilder()
                ._issuerConnector_(URI.create("http://example.org#connector"))
                ._issued_(now)
                ._modelVersion_("4.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()
                        ._tokenFormat_(TokenFormat.JWT)
                        ._tokenValue_("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHQifQ.eyJ...")
                        .build())
                ._senderAgent_(URI.create("http://example.org#senderAgent"))
                ._recipientConnector_(Util.asList(URI.create("http://example.org#recipientConnector1"), URI.create("http://example.org#recipientConnector2")))
                .build();
    }

    private ResponseMessage buildResponseMessage() {
        final var now = IdsMessageUtils.getGregorianNow();
        return new ResponseMessageBuilder()
                ._issuerConnector_(URI.create("http://example.org#connector"))
                ._issued_(now)
                ._correlationMessage_(URI.create("http://message"))
                ._modelVersion_("4.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()
                        ._tokenFormat_(TokenFormat.JWT)
                        ._tokenValue_("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHQifQ.eyJ...")
                        .build())
                ._senderAgent_(URI.create("http://example.org#senderAgent"))
                ._recipientConnector_(Util.asList(URI.create("http://example.org#recipientConnector1"), URI.create("http://example.org#recipientConnector2")))
                .build();
    }

}
