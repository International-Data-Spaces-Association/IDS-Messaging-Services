package de.fraunhofer.ids.framework.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Message;
import okhttp3.Request;

public interface RequestBuilder {
    /**
     * @param message A Message from the IDS Infomation Model
     * @param target  An URI to which the message should be sent to
     *
     * @return okhttp Response (to be updated to Infomation Model Message)
     *
     * @throws IOException if serialization of Message Header is not successful.
     */
    Request build( Message message, URI target ) throws IOException;


    /**
     * @param message A Message from the IDS Infomation Model
     * @param target  An URI to which the message should be sent to
     * @param payload The serialized payload to be sent with the Message
     *
     * @return okhttp Response (to be updated to Infomation Model Message)
     *
     * @throws IOException if serialization of Message Header is not successful.
     */
    Request build( Message message, URI target, String payload ) throws IOException;
}
