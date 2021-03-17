package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.util.Optional;

import de.fraunhofer.iais.eis.DescriptionRequestMessage;

public class DescriptionRequestMAP implements MessageAndPayload<DescriptionRequestMessage, Void> {

    private DescriptionRequestMessage descriptionRequest;

    public DescriptionRequestMAP(DescriptionRequestMessage selfDescriptionRequest) {
        this.descriptionRequest = selfDescriptionRequest;
    }

    @Override
    public DescriptionRequestMessage getMessage() {
        return descriptionRequest;
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
