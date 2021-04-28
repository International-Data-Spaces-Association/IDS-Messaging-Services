package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceMAP implements MessageAndPayload<Message, Resource> {

    final Message  message;

    Resource resource;

    public ResourceMAP(final Message m) {
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
