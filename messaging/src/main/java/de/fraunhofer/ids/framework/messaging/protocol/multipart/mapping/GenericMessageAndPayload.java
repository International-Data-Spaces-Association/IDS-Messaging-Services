package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenericMessageAndPayload implements MessageAndPayload<Message, Object> {

    @Getter
    @NotNull
    Message message;

    Object payload;


    @Override
    public Optional<Object> getPayload() {
        return Optional.ofNullable(payload);
    }

    @Override
    public SerializedPayload serializePayload() throws IOException {
        SerializedPayload serializedPayload;
        if (Objects.nonNull(payload)) {
            serializedPayload = new SerializedPayload(new Serializer().serialize(payload).getBytes(), "application/ld+json");
        } else {
            serializedPayload =  SerializedPayload.EMPTY;
        }
        return serializedPayload;
    }
}
