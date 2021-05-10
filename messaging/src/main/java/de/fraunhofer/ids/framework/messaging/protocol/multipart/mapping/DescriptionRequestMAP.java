package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;

public class DescriptionRequestMAP implements MessageAndPayload<DescriptionRequestMessage, Void> {

    private DescriptionRequestMessage message;

    public DescriptionRequestMAP(DescriptionRequestMessage selfDescriptionRequest) {
        this.message = selfDescriptionRequest;
    }

    @Override
    public DescriptionRequestMessage getMessage() {
        return message;
    }

    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return SerializedPayload.EMPTY;
    }
}
