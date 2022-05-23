/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.ResultMessage;
import ids.messaging.protocol.multipart.MessageAndPayload;
import ids.messaging.protocol.multipart.SerializedPayload;
import lombok.RequiredArgsConstructor;

/**
 * MAP representing the ResultMessage.
 */
@RequiredArgsConstructor
public class ResultMAP implements MessageAndPayload<ResultMessage, String> {

    /**
     * The ResultMessage.
     */
    private final ResultMessage resultMessage;

    /**
     * The query result.
     */
    private final String queryResult;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultMessage getMessage() {
        return resultMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getPayload() {
        return Optional.of(queryResult);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(queryResult.getBytes(), "text/plain");
    }
}
