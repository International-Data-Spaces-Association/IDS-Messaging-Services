package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.util.Optional;

import de.fraunhofer.iais.eis.QueryMessage;
import lombok.Getter;

public class QueryMAP implements MessageAndPayload<QueryMessage, String> {

    @Getter
    private final QueryMessage message;
    private final String queryString;

    public QueryMAP(QueryMessage brokerQueryMessage, String queryString) {
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
