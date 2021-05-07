package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResultMAP implements MessageAndPayload<ResultMessage, String> {

    ResultMessage resultMessage;
    String        queryResult;

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
