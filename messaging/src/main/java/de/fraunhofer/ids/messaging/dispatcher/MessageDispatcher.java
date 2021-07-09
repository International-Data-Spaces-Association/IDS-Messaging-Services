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
package de.fraunhofer.ids.messaging.dispatcher;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsValidator;
import de.fraunhofer.ids.messaging.dispatcher.filter.PreDispatchingFilter;
import de.fraunhofer.ids.messaging.dispatcher.filter.PreDispatchingFilterException;
import de.fraunhofer.ids.messaging.dispatcher.filter.PreDispatchingFilterResult;
import de.fraunhofer.ids.messaging.handler.message.MessageAndClaimsHandler;
import de.fraunhofer.ids.messaging.handler.message.MessageHandler;
import de.fraunhofer.ids.messaging.handler.message.MessageHandlerException;
import de.fraunhofer.ids.messaging.handler.message.MessagePayloadInputstream;
import de.fraunhofer.ids.messaging.handler.request.RequestMessageHandler;
import de.fraunhofer.ids.messaging.response.ErrorResponse;
import de.fraunhofer.ids.messaging.response.MessageResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * The MessageDispatcher takes all incoming Messages, applies all defined PreDispatchingFilters onto them,
 * checks the DAPS token, gives Messages to the specified MessageHandlers depending on their type and returns
 * the results returned by the MessageHandlers.
 */
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MessageDispatcher {
    ObjectMapper               objectMapper;
    List<PreDispatchingFilter> preDispatchingFilters;
    RequestMessageHandler      requestMessageHandler;
    ConfigContainer            configContainer;
    DapsValidator              dapsValidator;

    /**
     * Create a MessageDispatcher.
     *  @param objectMapper          a jackson objectmapper for (de)serializing objects
     * @param requestMessageHandler resolver for finding the fitting {@link MessageHandler} for the incoming Message
     * @param configContainer       the connector configuration
     * @param dapsValidator         validator class for daps dat
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
     * Register a new PreDispatchingFilter which will be used to filter incoming messages.
     *
     * @param preDispatchingFilter a new {@link PreDispatchingFilter} that should be added to the list of filters
     */
    public void registerPreDispatchingAction(final PreDispatchingFilter preDispatchingFilter) {
        this.preDispatchingFilters.add(preDispatchingFilter);
    }

    /**
     * Apply the preDispatchingFilters to the message. If it wasn't filtered: find the {@link MessageHandler} for its type.
     * Let the handler handle the Message and return the {@link MessageResponse}.
     *
     * @param header  header of the incoming Message (RequestMessage implementation)
     * @param payload payload of the incoming Message
     * @param <R>     a subtype of RequestMessage
     * @return the {@link MessageResponse} that is returned by the specified {@link MessageHandler} for the type of the incoming Message
     * @throws PreDispatchingFilterException if an error occurs in a PreDispatchingFilter
     */
    @SuppressWarnings("unchecked")
    public <R extends Message> MessageResponse process(final R header, final InputStream payload)
            throws PreDispatchingFilterException {
        final var connectorId = configContainer.getConnector().getId();
        final var modelVersion = configContainer.getConnector().getOutboundModelVersion();

        //check dat and cache claims
        Optional<Jws<Claims>> optionalClaimsJws = Optional.empty();
        if (configContainer.getConfigurationModel().getConnectorDeployMode() == ConnectorDeployMode.PRODUCTIVE_DEPLOYMENT) {
            try {
                final var claims = dapsValidator.getClaims(header.getSecurityToken());
                optionalClaimsJws = Optional.ofNullable(claims);
                if(!dapsValidator.checkClaims(claims, null)){
                    return ErrorResponse.withDefaultHeader(
                            RejectionReason.NOT_AUTHORIZED, "DAT could not be verified!", connectorId,
                            modelVersion, header.getId());
                }
            } catch (ClaimsException e) {
                return ErrorResponse.withDefaultHeader(
                        RejectionReason.NOT_AUTHORIZED, "Claims of DAT could not be parsed!", connectorId,
                        modelVersion, header.getId());
            }
        }


        //apply all preDispatchingFilters to the message
        for (final var preDispatchingFilter : this.preDispatchingFilters) {
            if (log.isDebugEnabled()) {
                log.debug("Applying a preDispatchingFilter");
            }

            try {
                final var result = preDispatchingFilter.process(header);
                if (!result.isSuccess()) {
                    if (log.isDebugEnabled()) {
                        log.debug("A preDispatchingFilter failed!");
                    }

                    if (log.isErrorEnabled()) {
                        log.error(result.getMessage(), result.getError());
                    }

                    return ErrorResponse.withDefaultHeader(
                            RejectionReason.MALFORMED_MESSAGE, result.getMessage(), connectorId,
                            modelVersion, header.getId());
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("A preDispatchingFilter threw an exception!");
                    log.debug(e.getMessage(), e);
                }

                throw new PreDispatchingFilterException(e);
            }
        }

        // Returns the MessageHandler of a given MessageType of the header-part.
        // The MessageType is a subtype of RequestMessage.class from Infomodel.
        final var resolvedHandler = requestMessageHandler.resolveHandler(header.getClass());

        // Checks if revolvedHandler is not null
        if (resolvedHandler.isPresent()) {

            //if an handler exists, let the handle handle the message and return its response
            try {
                final var handler = (MessageHandler<R>) resolvedHandler.get();
                if(handler instanceof MessageAndClaimsHandler){
                    return ((MessageAndClaimsHandler) handler).handleMessage(header, new MessagePayloadInputstream(payload, objectMapper), optionalClaimsJws);
                }else{
                    return handler.handleMessage(header, new MessagePayloadInputstream(payload, objectMapper));
                }
            } catch (MessageHandlerException e) {
                if (log.isDebugEnabled()) {
                    log.debug("The message handler threw an exception!");
                }

                return ErrorResponse.withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR,
                                                       "Error while handling the request!", connectorId, modelVersion,
                                                       header.getId());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("No message handler exists for %s", header.getClass()));
            }

            //If no handler for the type exists, the message type isn't supported
            return ErrorResponse.withDefaultHeader(RejectionReason.MESSAGE_TYPE_NOT_SUPPORTED,
                                                   "No handler for provided message type was found!", connectorId,
                                                   modelVersion, header.getId());
        }
    }
}
