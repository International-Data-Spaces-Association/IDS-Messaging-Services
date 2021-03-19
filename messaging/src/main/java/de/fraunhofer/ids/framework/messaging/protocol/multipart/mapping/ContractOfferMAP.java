package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferMessage;

import java.util.Optional;

public class ContractOfferMAP extends ContractMAP<ContractOfferMessage, ContractOffer> {


    public ContractOfferMAP(ContractOfferMessage contractOfferMessage, ContractOffer payload) {
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
