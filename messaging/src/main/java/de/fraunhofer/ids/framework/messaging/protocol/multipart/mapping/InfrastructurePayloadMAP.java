package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;


import java.util.Optional;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@RequiredArgsConstructor
public class InfrastructurePayloadMAP implements MessageAndPayload<Message, InfrastructureComponent> {

    @Getter
    @NotNull
    private final Message                 message;

    private       InfrastructureComponent connectorSelfDescription;


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
