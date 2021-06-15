package de.fraunhofer.ids.messaging.util;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.Util;
import okhttp3.MediaType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class InfomodelMessageBuilderTest {

    @Test
    public void testBuildMessage() throws IOException {
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