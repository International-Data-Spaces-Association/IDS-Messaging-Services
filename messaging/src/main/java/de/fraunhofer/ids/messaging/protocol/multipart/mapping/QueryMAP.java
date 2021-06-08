package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class QueryMAP implements MessageAndPayload<QueryMessage, String> {

    @Getter
    QueryMessage message;

    String queryString;

    @Override
    public Optional<String> getPayload() {
        return Optional.of(queryString);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(queryString.getBytes(), "text/plain");
    }
}
