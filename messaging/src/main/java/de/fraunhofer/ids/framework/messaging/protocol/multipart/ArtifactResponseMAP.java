package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.io.File;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Slf4j
public class ArtifactResponseMAP implements MessageAndPayload<ArtifactResponseMessage, File> {


    private final ArtifactResponseMessage artifactResponseMessage;
    private final File payload;

    public ArtifactResponseMAP(ArtifactResponseMessage artifactResponseMessage, File payload) {
        this.artifactResponseMessage = artifactResponseMessage;
        this.payload = payload;
    }

    @Override
    public ArtifactResponseMessage getMessage() {
        return artifactResponseMessage;
    }

    @Override
    public Optional<File> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        try {
            return new SerializedPayload(Files.readAllBytes(payload.toPath()), "application/octet-stream", payload.getName());
        }
        catch (IOException e) {
            log.error("Could not serialize file", e);
            return SerializedPayload.EMPTY;
        }
    }
}