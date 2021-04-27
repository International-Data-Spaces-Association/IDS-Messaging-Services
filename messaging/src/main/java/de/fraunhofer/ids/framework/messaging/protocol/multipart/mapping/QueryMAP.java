package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;
import lombok.Getter;

public class QueryMAP implements MessageAndPayload<QueryMessage, String> {

    @Getter
    private final QueryMessage message;
    private final String queryString;

    public QueryMAP(final QueryMessage brokerQueryMessage, final String queryString) {
        this.message = brokerQueryMessage;
        this.queryString = queryString;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.of(queryString);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(queryString.getBytes(), "text/plain");
    }
}
