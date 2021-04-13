package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;

public class ResultMAP implements MessageAndPayload<ResultMessage, String> {

    private final ResultMessage message;
    private final String        queryResult;

    public ResultMAP( ResultMessage message, String queryResult ) {
        this.message = message;
        this.queryResult = queryResult;
    }

    @Override
    public ResultMessage getMessage() {
        return message;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.of(queryResult);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(queryResult.getBytes(), "text/plain");
    }
}
