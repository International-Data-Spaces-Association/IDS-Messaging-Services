package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InfrastructurePayloadMAP implements MessageAndPayload<Message, InfrastructureComponent> {

    @Getter
    @NotNull
    Message message;

    InfrastructureComponent connectorSelfDescription;


    @Override
    public Optional<InfrastructureComponent> getPayload() {
        return Optional.ofNullable(connectorSelfDescription);
    }

    @Override
    public SerializedPayload serializePayload() {
        if (connectorSelfDescription != null) {
            return new SerializedPayload(connectorSelfDescription.toRdf().getBytes(), "application/ld+json");
        } else {
            return SerializedPayload.EMPTY;
        }
    }
}
