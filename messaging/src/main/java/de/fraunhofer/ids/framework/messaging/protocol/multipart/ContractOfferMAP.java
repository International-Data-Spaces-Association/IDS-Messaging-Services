package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferImpl;
import de.fraunhofer.iais.eis.ContractOfferMessage;

import java.util.Optional;

public class ContractOfferMAP extends ContractMAP<ContractOfferMessage, ContractOffer> {


    public ContractOfferMAP(ContractOfferMessage contractOfferMessage, ContractOffer payload) {
        this.contractMessage = contractOfferMessage;
        this.payload = payload;
    }

    @Override
    public ContractOfferMessage getMessage() {
        return contractMessage;
    }

    @Override
    public Optional<Contract> getPayload() {
        return Optional.of(payload);
    }

}
