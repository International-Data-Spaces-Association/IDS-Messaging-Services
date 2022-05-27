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
package ids.messaging.protocol.multipart;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Class providing the serialized payload.
 */
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SerializedPayload {

    /**
     * Default SerializedPayload.
     */
    public static final SerializedPayload EMPTY = new SerializedPayload();

    /**
     * The serialized payload.
     */
    private byte[] serialization;

    /**
     * The content type.
     */
    @Setter(AccessLevel.NONE)
    private String contentType;

    /**
     * The filename.
     */
    private String filename;

    /**
     * Constructor for SerializedPayload.
     *
     * @param serialization The serialized payload.
     */
    public SerializedPayload(final byte... serialization) {
        this.serialization = serialization;
    }

    /**
     * Constructor for SerializedPayload.
     *
     * @param serialization The serialized payload.
     * @param contentType The content type of the payload.
     */
    public SerializedPayload(final byte[] serialization, final String contentType) {
        this.serialization = serialization;
        this.contentType = contentType;
    }
}
