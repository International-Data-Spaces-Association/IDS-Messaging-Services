package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractRequest;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;

public class ContractRequestMAP extends AbstractContractMAP<ContractRequestMessage, ContractRequest> {

    public ContractRequestMAP(final ContractRequestMessage contractRequestMessage,
                              final ContractRequest payload) {
        this.message = contractRequestMessage;
        this.payload = payload;
    }

    @Override
    public ContractRequestMessage getMessage() {
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
