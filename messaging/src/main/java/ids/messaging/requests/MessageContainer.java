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
package ids.messaging.requests;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import lombok.Getter;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Container holding ids header fields, the underlying IDS message they were parsed from
 * and the message payload.
 *
 * @param <T> Type of payload.
 */
@Getter
public class MessageContainer<T> {

    /**
     * Container holding IDS Message HTTP header fields.
     */
    private final HeaderContainer headerContainer;

    /**
     * Payload received from response.
     */
    private final T receivedPayload;

    /**
     * Received message header als {@link Message} object.
     */
    private final Message underlyingMessage;

    /**
     * Optional: RejectionReason, if received message was a {@link RejectionMessage}.
     */
    private final Optional<RejectionReason> rejectionReason;

    /**
     * Construct a MessageContainer from incoming MAP.
     *
     * @param message Incoming message header.
     * @param payload Incoming message payload.
     */
    public MessageContainer(final Message message, final T payload) {
        this.receivedPayload = payload;
        this.underlyingMessage = message;
        this.headerContainer = containerFromMessage(underlyingMessage);
        rejectionReason = underlyingMessage instanceof RejectionMessage
                ? Optional.of(((RejectionMessage) underlyingMessage).getRejectionReason())
                : Optional.empty();
    }

    /**
     * Construct a MessageContainer from incoming MAP.
     *
     * @param headers Incoming map from http headers.
     * @param payload Incoming message payload.
     */
    public MessageContainer(final Map<String, String> headers, final T payload) {
        //TODO build headerContainer from httpHeaders given
        // in header map (used for incoming IDS_LDP messages)
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    /**
     * Check if message was a RejectionMessage.
     *
     * @return True, if this container saves a RejectionMessage.
     */
    public boolean isRejection() {
        return rejectionReason.isPresent();
    }

    /**
     * Get ID of incoming message.
     *
     * @return The message-ID.
     */
    public URI getMessageID() {
        return underlyingMessage.getId();
    }

    /**
     * Extract headers from incoming message.
     *
     * @param message Incoming message header.
     * @return {@link HeaderContainer} from extracted header fields.
     */
    private HeaderContainer containerFromMessage(final Message message) {
        return new HeaderContainer(
                Optional.ofNullable(message.getAuthorizationToken()),
                message.getSecurityToken(),
                message.getIssued(),
                message.getCorrelationMessage(),
                Optional.ofNullable(message.getContentVersion()),
                message.getIssuerConnector(),
                message.getSenderAgent(),
                message.getRecipientAgent() != null ? message.getRecipientAgent() : List.of(),
                Optional.ofNullable(message.getTransferContract()),
                message.getContentVersion()
        );
    }
}
