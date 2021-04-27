package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SerializedPayload {

    public static final SerializedPayload EMPTY = new SerializedPayload();

    private byte[] serialization;

    @Setter(AccessLevel.NONE)
    private String contentType;

    private String filename;


    public SerializedPayload(final byte... serialization) {
        this.serialization = serialization;
    }

    public SerializedPayload(final byte[] serialization, final String contentType) {
        this.serialization = serialization;
        this.contentType = contentType;
    }
}
