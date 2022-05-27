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

import java.io.File;
import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.util.Util;
import ids.messaging.common.SerializeException;
import okhttp3.MediaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class InfomodelMessageBuilderTest {

    @Test
    void testBuildMessage() throws IOException, SerializeException {
        assertNotNull(InfomodelMessageBuilder.messageWithFile(buildArtifactRequestMessage(), File.createTempFile("temp", "file"), MediaType.parse("image/jpeg")));
        assertNotNull(InfomodelMessageBuilder.messageWithString(buildArtifactRequestMessage(), "String payload!"));
    }

    private RequestMessage buildArtifactRequestMessage() {
        final var now = IdsMessageUtils.getGregorianNow();
        return new ArtifactRequestMessageBuilder()
                ._issuerConnector_(URI.create("http://example.org#connector"))
                ._issued_(now)
                ._requestedArtifact_(URI.create("http://example.artifact"))
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
