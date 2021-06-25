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
package de.fraunhofer.ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.RejectionMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class InfrastructureService  {
    ConfigContainer   container;
    DapsTokenProvider tokenProvider;
    MessageService    messageService;

    /**
     * {@inheritDoc}
     */
    public DescriptionResponseMAP requestSelfDescription(final URI uri) throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {
        final var header =   new DescriptionRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
        final var messageAndPayload = new GenericMessageAndPayload(header);
        final var response = messageService.sendIdsMessage(messageAndPayload, uri);

        return expectDescriptionResponseMAP(response);

    }

    /**
     * casts generic {@link MessageAndPayload} to {@link DescriptionResponseMAP}.
     *
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}
     * @return {@link MessageAndPayload object specialized to the expected Message}
     * @throws UnexpectedResponseException if a rejection message or any other unexpected message was returned.
     */
    private DescriptionResponseMAP expectDescriptionResponseMAP(final MessageAndPayload<?, ?> response)
            throws UnexpectedResponseException {
        if (response instanceof DescriptionResponseMAP) {
            return (DescriptionResponseMAP) response;
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new UnexpectedResponseException("Message rejected by target with following Reason: " + rejectionMessage.getRejectionReason());
        }

        throw new UnexpectedResponseException(String.format("Unexpected Message of type %s was returned", response.getMessage().getClass().toString()));
    }

    /**
     * Casts generic {@link MessageAndPayload} to {@link MessageProcessedNotificationMAP}.
     *
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}
     * @return {@link MessageAndPayload object specialized to the expected Message}
     * @throws UnexpectedResponseException if a rejection message or any other unexpected message was returned.
     */
    protected MessageProcessedNotificationMAP expectMessageProcessedNotificationMAP(final MessageAndPayload<?, ?> response)
            throws UnexpectedResponseException {

        if (response instanceof MessageProcessedNotificationMAP) {
            return (MessageProcessedNotificationMAP) response;
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new UnexpectedResponseException("Message rejected by target with following Reason: " + rejectionMessage.getRejectionReason());
        }

        throw new UnexpectedResponseException(String.format("Unexpected Message of type %s was returned", response.getMessage().getClass().toString()));
    }

    /**
     * Casts generic {@link MessageAndPayload} to {@link ResultMAP}.
     *
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}
     * @return {@link MessageAndPayload object specialized to the expected Message}
     * @throws UnexpectedResponseException if a rejection message or any other unexpected message was returned.
     */
    protected ResultMAP expectResultMAP(final MessageAndPayload<?, ?> response) throws UnexpectedResponseException {
        if (response instanceof ResultMAP) {
            return (ResultMAP) response;
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new UnexpectedResponseException("Message rejected by target with following Reason: " + rejectionMessage.getRejectionReason());
        }

        throw new UnexpectedResponseException(String.format("Unexpected Message of type %s was returned", response.getMessage().getClass().toString()));
    }

}
