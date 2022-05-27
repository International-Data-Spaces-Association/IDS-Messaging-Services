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
package ids.messaging.dispatcher;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsValidator;
import ids.messaging.dispatcher.filter.PreDispatchingFilter;
import ids.messaging.dispatcher.filter.PreDispatchingFilterException;
import ids.messaging.handler.message.MessageAndClaimsHandler;
import ids.messaging.handler.message.MessageHandler;
import ids.messaging.handler.message.MessageHandlerException;
import ids.messaging.handler.message.MessagePayloadInputstream;
import ids.messaging.handler.request.RequestMessageHandler;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * The MessageDispatcher takes all incoming Messages, applies all defined PreDispatchingFilters
 * onto them, checks the DAPS token, gives Messages to the specified MessageHandlers depending
 * on their type and returns the results returned by the MessageHandlers.
 */
@Slf4j
public class MessageDispatcher {

    /**
     * Flag for checking referredConnector.
     */
    @Value("${referred.check:false}")
    private boolean referringCheck;

    /**
     * The ObjectMapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * The PreDispatchingFilter.
     */
    private final List<PreDispatchingFilter> preDispatchingFilters;

    /**
     * The RequestMessageHandler.
     */
    private final RequestMessageHandler requestMessageHandler;

    /**
     * The ConfigContainer.
     */
    private final ConfigContainer configContainer;

    /**
     * The DapsValidator.
     */
    private final DapsValidator dapsValidator;

    /**
     * Create a MessageDispatcher.
     *
     * @param objectMapper A jackson objectmapper for (de)serializing objects.
     * @param requestMessageHandler Resolver for finding the fitting {@link MessageHandler} for
     *                              the incoming Message.
     * @param configContainer The connector configuration.
     * @param dapsValidator Validator class for DAPS DAT.
     */
    public MessageDispatcher(final ObjectMapper objectMapper,
                             final RequestMessageHandler requestMessageHandler,
                             final ConfigContainer configContainer,
                             final DapsValidator dapsValidator) {
        this.objectMapper = objectMapper;
        this.requestMessageHandler = requestMessageHandler;
        this.configContainer = configContainer;
        this.dapsValidator = dapsValidator;
        this.preDispatchingFilters = new LinkedList<>();
    }

    /**
     * Register a new PreDispatchingFilter which will
     * be used to filter incoming messages.
     *
     * @param preDispatchingFilter A new {@link PreDispatchingFilter} that should be added to
     *                             the list of filters.
     */
    public void registerPreDispatchingAction(
            final PreDispatchingFilter preDispatchingFilter) {
        this.preDispatchingFilters.add(preDispatchingFilter);
    }

    /**
     * Apply the preDispatchingFilters to the message. If it wasn't filtered:
     * find the {@link MessageHandler} for its type. Let the handler handle the Message and return
     * the {@link MessageResponse}.
     *
     * @param header Header of the incoming Message (RequestMessage implementation).
     * @param payload Payload of the incoming Message.
     * @param <R> A subtype of RequestMessage.
     * @return The {@link MessageResponse} that is returned by the specified {@link MessageHandler}
     * for the type of the incoming Message.
     * @throws PreDispatchingFilterException If an error occurs
     * in a PreDispatchingFilter.
     */
    @SuppressWarnings("unchecked")
    public <R extends Message> MessageResponse process(final R header,
                                               final InputStream payload)
            throws PreDispatchingFilterException {
        final var connectorId = configContainer.getConnector().getId();
        final var modelVersion =
                configContainer.getConnector().getOutboundModelVersion();

        //check dat and save token claims
        Optional<Jws<Claims>> optionalClaimsJws = Optional.empty();
        if (configContainer.getConfigurationModel().getConnectorDeployMode()
           == ConnectorDeployMode.PRODUCTIVE_DEPLOYMENT) {
            try {
                final var claims =
                        dapsValidator.getClaims(header.getSecurityToken());

                if (referringCheck && !isReferringConnector(header, claims)) {
                    return ErrorResponse.withDefaultHeader(
                            RejectionReason.BAD_PARAMETERS,
                            "ids:issuerConnector in message-header"
                            + " (" + header.getIssuerConnector() + ") does not match"
                            + " referringConnector in body of DAT claims"
                            + " (" + claims.getBody().get("referringConnector") + ")!",
                            connectorId,
                            modelVersion, header.getId());
                }

                optionalClaimsJws = Optional.ofNullable(claims);

                if (!dapsValidator.checkClaims(claims, null)) {
                    return ErrorResponse.withDefaultHeader(
                            RejectionReason.NOT_AUTHORIZED,
                            "DAT could not be verified!",
                            connectorId,
                            modelVersion, header.getId());
                }
            } catch (ClaimsException e) {
                return ErrorResponse.withDefaultHeader(
                        RejectionReason.NOT_AUTHORIZED,
                        "Claims of DAT could not be parsed!",
                        connectorId,
                        modelVersion,
                        header.getId());
            }
        }

        //apply all preDispatchingFilters to the message
        for (final var preDispatchingFilter : this.preDispatchingFilters) {
            if (log.isDebugEnabled()) {
                log.debug("Applying a preDispatchingFilter... [code=(IMSMED0115)]");
            }

            try {
                final var result = preDispatchingFilter.process(header);
                if (!result.isSuccess()) {
                    if (log.isErrorEnabled()) {
                        log.error("A preDispatchingFilter failed, sending"
                              + " response RejectionReason.MALFORMED_MESSAGE! [code=(IMSMEE0019),"
                              + " result=({})]", result.getMessage());
                    }

                    return ErrorResponse.withDefaultHeader(
                            RejectionReason.MALFORMED_MESSAGE,
                            result.getMessage(),
                            connectorId,
                            modelVersion, header.getId());
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("A preDispatchingFilter threw an exception! [code=(IMSMED0116),"
                              + " exception=({})]", e.getMessage());
                }

                throw new PreDispatchingFilterException(e);
            }
        }

        // Returns the MessageHandler of a given MessageType of the header-part.
        // The MessageType is a subtype of RequestMessage.class from Infomodel.
        final var resolvedHandler =
                requestMessageHandler.resolveHandler(header.getClass());

        // Checks if revolvedHandler is not null
        if (resolvedHandler.isPresent()) {
            //if an handler exists, let the handle handle the
            // message and return its response
            try {
                final var handler = (MessageHandler<R>) resolvedHandler.get();
                if (handler instanceof MessageAndClaimsHandler) {
                    //for MessageAndClaims handlers, also pass parsed DAT claims
                    return ((MessageAndClaimsHandler) handler)
                        .handleMessage(header,
                           new MessagePayloadInputstream(payload, objectMapper),
                           optionalClaimsJws);
                } else {
                    return handler.handleMessage(header,
                                                 new MessagePayloadInputstream(
                                                         payload,
                                                         objectMapper));
                }
            } catch (MessageHandlerException e) {
                if (log.isDebugEnabled()) {
                    log.debug("The message handler threw an exception! [code=(IMSMED0117)]");
                }

                return ErrorResponse.withDefaultHeader(
                        RejectionReason.INTERNAL_RECIPIENT_ERROR,
                        "Error while handling the request!", connectorId,
                        modelVersion,
                        header.getId());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No message handler exists! [code=(IMSMED0118), type=({})]",
                          header.getClass());
            }

            //If no handler for the type exists, the message type isn't supported
            return ErrorResponse.withDefaultHeader(
                    RejectionReason.MESSAGE_TYPE_NOT_SUPPORTED,
                    "No handler for provided message type was found!",
                    connectorId,
                    modelVersion, header.getId());
        }
    }

    private <R extends Message> boolean isReferringConnector(final R header,
                                                             final Jws<Claims> claims) {
        final var datClaim = claims.getBody().get("referringConnector").toString().strip();
        final var issuer = header.getIssuerConnector().toString().strip();

        return datClaim.equals(issuer);
    }
}
