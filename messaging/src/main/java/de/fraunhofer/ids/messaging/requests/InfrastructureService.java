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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.MessageBuilderException;
import de.fraunhofer.ids.messaging.common.SerializeException;
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
            SendMessageException,
            MessageBuilderException {
        try {
            final var header = new DescriptionRequestMessageBuilder()
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._modelVersion_(
                            container.getConnector().getOutboundModelVersion())
                    ._issuerConnector_(container.getConnector().getId())
                    ._senderAgent_(container.getConnector().getId())
                    ._securityToken_(tokenProvider.getDAT())
                    .build();
            final var messageAndPayload = new GenericMessageAndPayload(header);
            final var response =
                    messageService.sendIdsMessage(messageAndPayload, uri);

            return expectMapOfTypeT(response, DescriptionResponseMAP.class);
        } catch (ConstraintViolationException constraintViolationException) {
            throw new MessageBuilderException(constraintViolationException);
        }
    }

    /**
     * Request an Artifact from another Connector
     *
     * @param uri uri of target connector
     * @param requestedArtifact artifact which is requested
     * @return ArtifactResponseMAP from response, if response is received correctly
     * @throws IOException if Response Cannot be parsed
     * @throws DapsTokenManagerException if there is an error when getting a DAT Token
     * @throws MultipartParseException if response cannot be parsed to multipart map
     * @throws ClaimsException when DAT of incoming response is rejected
     */
    public MaybeMAP<ArtifactResponseMAP, MessageProcessedNotificationMAP> requestArtifact(final URI uri, final URI requestedArtifact)
            throws ClaimsException, IOException, DapsTokenManagerException, MultipartParseException {

        final var header = new ArtifactRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._requestedArtifact_(requestedArtifact)
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
        final var messageAndPayload = new GenericMessageAndPayload(header);
        final var response = messageService.sendIdsMessage(messageAndPayload, uri);

        return expectMaybeMapOfTypeT(response, ArtifactResponseMAP.class);
    }

    /**
     * Check if incoming response if of expected type, throw an IOException with information, if it is not
     *
     * @param response incoming response MAP
     * @param expectedType Expected type response MAP should have
     * @param <T> expected Type as generic
     * @return response MAP cast to expected type (if it can be cast to type)
     * @throws IOException if response cannot be cast to expected type
     */
    protected <T extends MessageAndPayload<?,?>> MaybeMAP<T, MessageProcessedNotificationMAP> expectMaybeMapOfTypeT(
            final MessageAndPayload<?, ?> response,
            final Class<T> expectedType) throws IOException {

        if (expectedType.isAssignableFrom(response.getClass())){
            return new MaybeMAP<>(expectedType.cast(response), null);
        }
        if(response instanceof MessageProcessedNotificationMAP){
            return new MaybeMAP(null, response);
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new IOException(
                    String.format(
                            "Message rejected by target with following Reason: %s",
                            rejectionMessage.getRejectionReason()
                    )
            );
        }
        throw new IOException(
                String.format(
                        "Unexpected Message of type %s was returned, expected Message of type %s",
                        response.getMessage().getClass().toString(), expectedType.getSimpleName()
                )
        );
    }

    /**
     * Check if incoming response if of expected type, throw an IOException with information, if it is not
     *
     * @param response incoming response MAP
     * @param expectedType Expected type response MAP should have
     * @param <T> expected Type as generic
     * @return response MAP cast to expected type (if it can be cast to type)
     * @throws IOException if response cannot be cast to expected type
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}
     * @return {@link MessageAndPayload object specialized to the expected Message}
     * @throws UnexpectedResponseException if a rejection message or any other unexpected message was returned.
     */
    protected <T extends MessageAndPayload<?,?>> T expectMapOfTypeT(
            final MessageAndPayload<?, ?> response,
            final Class<T> expectedType) throws IOException {

        if (expectedType.isAssignableFrom(response.getClass())){
            return expectedType.cast(response);
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new IOException(
                    String.format(
                            "Message rejected by target with following Reason: %s",
                            rejectionMessage.getRejectionReason()
                    )
            );
        }
        throw new IOException(
                String.format(
                        "Unexpected Message of type %s was returned, expected Message of type %s",
                        response.getMessage().getClass().toString(), expectedType.getSimpleName()
                )
        );
    }

    @Getter
    public static class MaybeMAP<K extends MessageAndPayload<?,?>, V extends MessageAndPayload<?,?>>{

        private final Optional<K> expectedMAP;
        private final Optional<V> alternativeMAP;

        public MaybeMAP(K expectedMAP, V alternativeMAP){
            this.expectedMAP = Optional.ofNullable(expectedMAP);
            this.alternativeMAP = Optional.ofNullable(alternativeMAP);
        }
    }

}
