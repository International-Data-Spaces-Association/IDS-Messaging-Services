package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractResponseMessage;

import java.util.Optional;

public class ContractResponseMAP extends ContractMAP<ContractResponseMessage, ContractOffer> {

    public ContractResponseMAP(ContractResponseMessage contractResponseMessage, ContractOffer payload) {
        this.contractMessage = contractResponseMessage;
        this.payload = payload;
    }

    @Override
    public ContractResponseMessage getMessage() {
        return contractMessage;
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
