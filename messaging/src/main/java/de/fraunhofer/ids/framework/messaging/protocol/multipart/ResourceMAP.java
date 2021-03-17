package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;

import java.util.Optional;

public class ResourceMAP implements MessageAndPayload<Message, Resource> {
    private Message message;
    private Resource resource;
    public ResourceMAP(Message m)
    {
        this.message = m;
    }

    public ResourceMAP(Message m, Resource r)
    {
        this.message = m;
        resource = r;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public Optional<Resource> getPayload() {
        if(resource == null)
        {
            return Optional.empty();
        }
        return Optional.of(resource);
    }

    @Override
    public SerializedPayload serializePayload() {
        if (resource != null) {
            return new SerializedPayload(resource.toRdf().getBytes(), "application/ld+json");
        }
        else return SerializedPayload.EMPTY;
    }

}
