package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractResponseMessage;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;

public class ContractResponseMAP extends AbstractContractMAP<ContractResponseMessage, ContractOffer> {

    public ContractResponseMAP(final ContractResponseMessage contractResponseMessage,
                               final ContractOffer payload) {
        this.message = contractResponseMessage;
        this.payload = payload;
    }

    @Override
    public ContractResponseMessage getMessage() {
        return message;
    }

    @Override
    public Optional<Contract> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.toRdf().getBytes(), "application/ld+json", payload.getId().toString());
    }
}
