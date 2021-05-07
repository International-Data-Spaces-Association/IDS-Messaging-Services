package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferMessage;

public class ContractOfferMAP extends AbstractContractMAP<ContractOfferMessage, ContractOffer> {

    public ContractOfferMAP(final ContractOfferMessage contractOfferMessage,
                            final ContractOffer payload) {
        this.message = contractOfferMessage;
        this.payload = payload;
    }

    @Override
    public ContractOfferMessage getMessage() {
        return message;
    }

    @Override
    public Optional<Contract> getPayload() {
        return Optional.of(payload);
    }

}
