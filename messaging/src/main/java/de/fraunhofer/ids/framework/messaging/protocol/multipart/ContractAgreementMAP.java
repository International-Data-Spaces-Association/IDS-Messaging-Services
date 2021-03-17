package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementMessage;

import java.util.Optional;

public class ContractAgreementMAP extends ContractMAP<ContractAgreementMessage, ContractAgreement> {



    public ContractAgreementMAP(ContractAgreementMessage contractAgreementMessage, ContractAgreement payload) {
        this.contractMessage = contractAgreementMessage;
        this.payload = payload;
    }

    @Override
    public ContractAgreementMessage getMessage() {
        return contractMessage;
    }

    @Override
    public Optional<Contract> getPayload() {
        return Optional.of(payload);
    }

}