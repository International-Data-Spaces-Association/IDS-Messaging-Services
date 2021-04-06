package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;

public class DescriptionResponseMAP implements MessageAndPayload<DescriptionResponseMessage, String> {

    private DescriptionResponseMessage message;
    private String payload;

    public DescriptionResponseMAP(DescriptionResponseMessage response, String payload) {
        this.message = response;
        this.payload = payload;
    }

    @Override
    public DescriptionResponseMessage getMessage() {
        return message;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.getBytes(), "application/ld+json");
    }

}