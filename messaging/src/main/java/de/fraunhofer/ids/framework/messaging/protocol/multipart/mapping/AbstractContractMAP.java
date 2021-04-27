package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;
import lombok.Getter;


public abstract class AbstractContractMAP<M extends Message, C extends Contract> implements MessageAndPayload<Message, Contract> {

    @Getter
    M message;

    C payload;

    @Override
    public Optional<Contract> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.toRdf().getBytes(), "application/ld+json", payload.getId().toString());
    }
}
