package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementMessage;


public class ContractAgreementMAP extends ContractMAP<ContractAgreementMessage, ContractAgreement> {

    public ContractAgreementMAP(ContractAgreementMessage contractAgreementMessage, ContractAgreement payload) {
        this.message = contractAgreementMessage;
        this.payload = payload;
    }

    @Override
    public ContractAgreementMessage getMessage() {
        return message;
    }

    @Override
    public Optional<Contract> getPayload() {
        return Optional.of(payload);
    }

}