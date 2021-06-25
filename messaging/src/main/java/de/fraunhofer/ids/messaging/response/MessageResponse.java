/*
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
 */
package de.fraunhofer.ids.messaging.response;

import java.io.IOException;
import java.util.Map;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.protocol.SerializeException;

/**
 * A MessageResponse is returned by the MessageHandlers, for easy building of Responses
 * and automatic creation of Maps to be returned as Multipart Responses.
 */
public interface MessageResponse {
    /**
     * Create an empty MessageResponse.
     *
     * @return an empty MessageResponse
     */
    static MessageResponse empty() {
        return s -> Map.of();
    }

    /**
     * Create a MultipartMap from the MessageResponse, with header and payload part.
     *
     * @param serializer a Serializer to produce JsonLD
     * @return Map of response parts that can be used for the multipart response
     * @throws SerializeException if some object cannot be serialized
     */
    Map<String, Object> createMultipartMap(Serializer serializer) throws SerializeException;
}
