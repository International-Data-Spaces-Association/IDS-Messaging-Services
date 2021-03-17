package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;

import java.util.Optional;

public class ArtifactRequestMAP implements MessageAndPayload<ArtifactRequestMessage, Void> {

    private final ArtifactRequestMessage artifactRequestMessage;

    public ArtifactRequestMAP(ArtifactRequestMessage artifactRequestMessage) {
        this.artifactRequestMessage = artifactRequestMessage;
    }

    @Override
    public ArtifactRequestMessage getMessage() {
        return artifactRequestMessage;
    }

    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return SerializedPayload.EMPTY;
    }
}
