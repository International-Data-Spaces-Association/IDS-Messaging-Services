package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SerializedPayload {
    public static SerializedPayload EMPTY = new SerializedPayload();
    private       byte[]            serialization;
    @Setter( AccessLevel.NONE )
    private       String            contentType;
    private       String            filename;

    public SerializedPayload( byte[] serialization ) {
        this.serialization = serialization;
    }

    public SerializedPayload( byte[] serialization, String contentType ) {
        this.serialization = serialization;
        this.contentType = contentType;
    }
}
