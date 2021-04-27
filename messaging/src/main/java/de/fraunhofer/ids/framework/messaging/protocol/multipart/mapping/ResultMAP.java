package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;


import java.util.Optional;

public class ResultMAP implements MessageAndPayload<ResultMessage, String> {

    private final ResultMessage resultMessage;
    private final String        queryResult;

    public ResultMAP(final ResultMessage resultMessage, final String queryResult) {
        this.resultMessage = resultMessage;
        this.queryResult = queryResult;
    }

    @Override
    public ResultMessage getMessage() {
        return resultMessage;
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
