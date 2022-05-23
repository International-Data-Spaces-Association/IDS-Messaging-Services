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

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.RejectionMessage;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.UnexpectedResponseException;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.MessageAndPayload;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.mapping.RejectionMAP;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.builder.IdsRequestBuilderService;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract infrastructureservice to request self descriptions.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class InfrastructureService  {

    /**
     * The ConfigContainer.
     */
    protected final ConfigContainer container;

    /**
     * The DapsTokenProvider.
     */
    protected final DapsTokenProvider tokenProvider;

    /**
     * The MessageService.
     */
    protected final MessageService messageService;

    /**
     * The IdsRequestBuilderService.
     */
    protected final IdsRequestBuilderService requestBuilderService;

    /**
     * {@inheritDoc}
     */
    public MessageContainer<?> requestSelfDescription(@NonNull final URI uri) throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService.newRequest()
                                    .subjectDescription()
                                    .useMultipart()
                                    .operationGet(null)
                                    .execute(uri);

    }

    /**
     * {@inheritDoc}
     */
    public MessageContainer<?> requestSelfDescription(@NonNull final URI uri,
                                                      final URI requestedElement)
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService.newRequest()
                                    .subjectDescription()
                                    .useMultipart()
                                    .operationGet(requestedElement)
                                    .execute(uri);

    }

    /**
     * Check if incoming response if of expected type, throw an IOException with
     * information, if it is not.
     *
     * @param response {@link MessageAndPayload} as returned by the {@link MessageService}.
     * @param expectedType Expected type response MAP should have.
     * @param <T> Expected Type as generic.
     * @return {@link MessageAndPayload Object specialized to the expected Message}.
     * @throws UnexpectedResponseException If a rejection message or any other unexpected
     * message was returned.
     */
    protected <T extends MessageAndPayload<?, ?>> T expectMapOfTypeT(
            final MessageAndPayload<?, ?> response,
            final Class<T> expectedType) throws UnexpectedResponseException {

        if (expectedType.isAssignableFrom(response.getClass())) {
            return expectedType.cast(response);
        }

        if (response instanceof RejectionMAP) {
            final var rejectionMessage = (RejectionMessage) response.getMessage();
            throw new UnexpectedResponseException(
                    String.format(
                            "Message rejected by target with following Reason: %s",
                            rejectionMessage.getRejectionReason()
                    )
            );
        }
        throw new UnexpectedResponseException(
                String.format(
                        "Unexpected Message of type %s was returned, expected Message of type %s",
                        response.getMessage().getClass().toString(), expectedType.getSimpleName()
                )
        );
    }

    /**
     * Prints a log, that the message header is going to be build next.
     */
    protected void logBuildingHeader() {
        if (log.isDebugEnabled()) {
            log.debug("Building message header... [code=(IMSMED0141)]");
        }
    }
}
