package de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.SerializedPayload;

import java.util.Optional;

public class ResourceMAP implements MessageAndPayload<Message, Resource> {

    private final Message  message;
    private       Resource resource;

    public ResourceMAP(final Message m)
    {
        this.message = m;
    }

    public ResourceMAP(final Message m, final Resource r) {
        this.message = m;
        resource = r;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public Optional<Resource> getPayload() {
        if (resource == null) {
            return Optional.empty();
        } else {
            return Optional.of(resource);
        }
    }

    @Override
    public SerializedPayload serializePayload() {
        if (resource != null) {
            return new SerializedPayload(resource.toRdf().getBytes(), "application/ld+json");
        } else {
            return SerializedPayload.EMPTY;
        }
    }

}
