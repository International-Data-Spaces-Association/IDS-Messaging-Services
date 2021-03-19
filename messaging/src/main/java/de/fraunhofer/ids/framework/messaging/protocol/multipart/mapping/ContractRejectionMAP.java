package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.ContractRejectionMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;

public class ContractRejectionMAP implements MessageAndPayload<ContractRejectionMessage, Void> {

    private final ContractRejectionMessage contractRejectionMessage;

    public ContractRejectionMAP(ContractRejectionMessage contractRejectionMessage) {
        this.contractRejectionMessage = contractRejectionMessage;
    }

    @Override
    public ContractRejectionMessage getMessage() {
        return contractRejectionMessage;
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