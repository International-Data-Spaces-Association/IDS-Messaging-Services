package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.util.Optional;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractRequest;
import de.fraunhofer.iais.eis.ContractRequestMessage;

public class ContractRequestMAP extends ContractMAP<ContractRequestMessage, ContractRequest> {


    public ContractRequestMAP(ContractRequestMessage contractRequestMessage, ContractRequest payload) {
        this.contractMessage = contractRequestMessage;
        this.payload = payload;
    }

    @Override
    public ContractRequestMessage getMessage() {
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