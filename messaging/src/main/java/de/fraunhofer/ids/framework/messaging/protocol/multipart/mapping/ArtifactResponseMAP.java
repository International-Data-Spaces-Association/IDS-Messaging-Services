package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.io.File;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ArtifactResponseMAP implements MessageAndPayload<ArtifactResponseMessage, File> {

    @Getter
    ArtifactResponseMessage message;

    File payload;


    @Override
    public Optional<File> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        try {
            return new SerializedPayload(Files.readAllBytes(payload.toPath()), "application/octet-stream", payload.getName());
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Could not serialize file", e);
            }

            return SerializedPayload.EMPTY;
        }
    }
}
