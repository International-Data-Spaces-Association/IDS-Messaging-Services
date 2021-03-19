package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@RequiredArgsConstructor
public class RejectionMAP implements MessageAndPayload<RejectionMessage, String> {


    @Getter
    @NonNull
    private RejectionMessage message;

    private String payload;


    @Override
    public Optional<String> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.getBytes(), "text/plain");
    }
}
