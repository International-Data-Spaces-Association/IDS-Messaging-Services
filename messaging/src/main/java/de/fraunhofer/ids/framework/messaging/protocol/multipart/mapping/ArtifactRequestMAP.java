package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class ArtifactRequestMAP implements MessageAndPayload<ArtifactRequestMessage, Void> {

    @Getter
    @NonNull
    private final ArtifactRequestMessage message;


    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return null;
    }

}
