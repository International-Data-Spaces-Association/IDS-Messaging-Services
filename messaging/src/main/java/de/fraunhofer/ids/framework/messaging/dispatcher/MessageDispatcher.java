package de.fraunhofer.ids.framework.messaging.dispatcher;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.ClaimsException;
import de.fraunhofer.ids.framework.daps.DapsPublicKeyProvider;
import de.fraunhofer.ids.framework.daps.DapsValidator;
import de.fraunhofer.ids.framework.daps.DapsVerifier;
import de.fraunhofer.ids.framework.messaging.dispatcher.filter.PreDispatchingFilter;
import de.fraunhofer.ids.framework.messaging.dispatcher.filter.PreDispatchingFilterException;
import de.fraunhofer.ids.framework.messaging.dispatcher.filter.PreDispatchingFilterResult;
import de.fraunhofer.ids.framework.messaging.handler.message.MessageHandler;
import de.fraunhofer.ids.framework.messaging.handler.message.MessageHandlerException;
import de.fraunhofer.ids.framework.messaging.handler.message.MessagePayloadInputstream;
import de.fraunhofer.ids.framework.messaging.handler.request.RequestMessageHandler;
import de.fraunhofer.ids.framework.messaging.response.ErrorResponse;
import de.fraunhofer.ids.framework.messaging.response.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * The MessageDispatcher takes all incoming Messages, applies all defined PreDispatchingFilters onto them,
 * checks the DAPS token, gives Messages to the specified MessageHandlers depending on their type and returns
 * the results returned by the MessageHandlers.
 */
@Slf4j
public class MessageDispatcher {
    private final ObjectMapper               objectMapper;
    private final List<PreDispatchingFilter> preDispatchingFilters;
    private final RequestMessageHandler      requestMessageHandler;
    private final ConfigContainer            configContainer;

    /**
     * Create a MessageDispatcher
     *
     * @param objectMapper          a jackson objectmapper for (de)serializing objects
     * @param requestMessageHandler resolver for finding the fitting {@link MessageHandler} for the incoming Message
     * @param provider              a provider that can access the public key of the DAPS
     * @param configContainer       the connector configuration
     */
    public MessageDispatcher( final ObjectMapper objectMapper,
                              final RequestMessageHandler requestMessageHandler,
                              final DapsPublicKeyProvider provider,
                              final ConfigContainer configContainer ) {
        this.objectMapper = objectMapper;
        this.requestMessageHandler = requestMessageHandler;
        this.configContainer = configContainer;
        this.preDispatchingFilters = new LinkedList<>();

        registerDatVerificationFilter(provider, configContainer);
    }


    /**
     * Incoming messages are checked for a valid DAT token
     *
     * @param provider               DAPS Public Key Provider
     * @param configurationContainer Configuration
     */
    private void registerDatVerificationFilter( final DapsPublicKeyProvider provider,
                                                final ConfigContainer configurationContainer ) {
        //add DAT verification as PreDispatchingFilter
        registerPreDispatchingAction(in -> {
            if( configurationContainer.getConfigurationModel().getConnectorDeployMode()
                == ConnectorDeployMode.TEST_DEPLOYMENT ) {
                return PreDispatchingFilterResult
                        .successResult("ConnectorDeployMode is Test. Skipping Token verification!");
            }
            try {
                var verified = DapsVerifier.verify(DapsValidator.getClaims(in.getSecurityToken(), provider.providePublicKey()));
                return PreDispatchingFilterResult.builder()
                                                 .withSuccess(verified)
                                                 .withMessage(
                                                         String.format("Token verification result is: %s", verified))
                                                 .build();
            } catch( ClaimsException e ) {
                return PreDispatchingFilterResult.builder()
                                                 .withSuccess(false)
                                                 .withMessage("Token could not be parsed!" + e.getMessage())
                                                 .build();
            }
        });
    }

    /**
     * Register a new PreDispatchingFilter which will be used to filter incoming messages.
     *
     * @param preDispatchingFilter a new {@link PreDispatchingFilter} that should be added to the list of filters
     */
    public void registerPreDispatchingAction( final PreDispatchingFilter preDispatchingFilter ) {
        this.preDispatchingFilters.add(preDispatchingFilter);
    }

    /**
     * Apply the preDispatchingFilters to the message. If it wasn't filtered: find the {@link MessageHandler} for its type.
     * Let the handler handle the Message and return the {@link MessageResponse}.
     *
     * @param header  header of the incoming Message (RequestMessage implementation)
     * @param payload payload of the incoming Message
     * @param <R>     a subtype of RequestMessage
     *
     * @return the {@link MessageResponse} that is returned by the specified {@link MessageHandler} for the type of the incoming Message
     *
     * @throws PreDispatchingFilterException if an error occurs in a PreDispatchingFilter
     */
    @SuppressWarnings( "unchecked" )
    public <R extends Message> MessageResponse process( final R header, final InputStream payload )
            throws PreDispatchingFilterException {
        var connectorId = configContainer.getConnector().getId();
        var modelVersion = configContainer.getConnector().getOutboundModelVersion();
        //apply all preDispatchingFilters to the message
        for( var preDispatchingFilter : this.preDispatchingFilters ) {
            log.debug("Applying a preDispatchingFilter");
            try {
                val result = preDispatchingFilter.process(header);
                if( !result.isSuccess() ) {
                    log.debug("A preDispatchingFilter failed!");
                    log.error(result.getMessage(), result.getError());

                    return ErrorResponse
                            .withDefaultHeader(RejectionReason.MALFORMED_MESSAGE, result.getMessage(), connectorId,
                                               modelVersion, header.getId());
                }
            } catch( Exception e ) {
                log.debug("A preDispatchingFilter threw an exception!");
                log.debug(e.getMessage(), e);
                throw new PreDispatchingFilterException(e);
            }
        }

        // Returns the MessageHandler of a given MessageType of the header-part.
        // The MessageType is a subtype of RequestMessage.class from Infomodel.
        var resolvedHandler = requestMessageHandler.resolveHandler(header.getClass());

        // Checks if revolvedHandler is not null
        if( resolvedHandler.isPresent() ) {
            //if an handler exists, let the handle handle the message and return its response
            try {
                var handler = (MessageHandler<R>) resolvedHandler.get();
                return handler.handleMessage(header, new MessagePayloadInputstream(payload, objectMapper));
            } catch( MessageHandlerException e ) {
                log.debug("The message handler threw an exception!");
                return ErrorResponse.withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR,
                                                       "Error while handling the request!", connectorId, modelVersion,
                                                       header.getId());
            }
        } else {
            log.debug(String.format("No message handler exists for %s", header.getClass()));
            //If no handler for the type exists, the message type isn't supported
            return ErrorResponse.withDefaultHeader(RejectionReason.MESSAGE_TYPE_NOT_SUPPORTED,
                                                   "No handler for provided message type was found!", connectorId,
                                                   modelVersion, header.getId());
        }
    }
}
