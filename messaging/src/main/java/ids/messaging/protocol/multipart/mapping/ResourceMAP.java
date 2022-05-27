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

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;
import ids.messaging.protocol.multipart.MessageAndPayload;
import ids.messaging.protocol.multipart.SerializedPayload;

/**
 * MAP representing the ResourceMAP.
 */
public class ResourceMAP implements MessageAndPayload<Message, Resource> {

    /**
     * The message.
     */
    private final Message  message;

    /**
     * The resource.
     */
    private Resource resource;

    /**
     * Constructor for the ResourceMAP.
     *
     * @param m The message.
     */
    public ResourceMAP(final Message m) {
        this.message = m;
    }

    /**
     * Constructor for the ResourceMAP.
     *
     * @param m The message.
     * @param r The resource.
     */
    public ResourceMAP(final Message m, final Resource r) {
        this.message = m;
        resource = r;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Resource> getPayload() {
        if (resource == null) {
            return Optional.empty();
        } else {
            return Optional.of(resource);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializedPayload serializePayload() {
        if (resource != null) {
            return new SerializedPayload(resource.toRdf().getBytes(), "application/ld+json");
        } else {
            return SerializedPayload.EMPTY;
        }
    }

}
