package de.fraunhofer.ids.framework.messaging.protocol.multipart;


import java.util.Optional;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class InfrastructureComponentMAP implements MessageAndPayload<Message, InfrastructureComponent> {

    @Getter
    private final Message                 message;
    private       InfrastructureComponent connectorSelfDescription;

    public InfrastructureComponentMAP(Message message) {
        this.message = message;
    }


    @Override
    public Optional<InfrastructureComponent> getPayload() {
        return Optional.ofNullable(connectorSelfDescription);
    }

    @Override
    public SerializedPayload serializePayload() {
        if (connectorSelfDescription != null) {
            return new SerializedPayload(connectorSelfDescription.toRdf().getBytes(), "application/ld+json");
        }
        else return SerializedPayload.EMPTY;
    }
}
