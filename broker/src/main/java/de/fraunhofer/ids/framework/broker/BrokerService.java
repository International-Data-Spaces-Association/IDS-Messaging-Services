package de.fraunhofer.ids.framework.broker;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.*;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Broker Communication Controller. Generates appropriate ids multipart messages and sends them to the broker
 * infrastructure api.
 **/
@Slf4j
@Service
public class BrokerService implements IDSBrokerService {
    private static final String     INFO_MODEL_VERSION = "4.0.0";
    private static final Serializer SERIALIZER         = new Serializer();

    private ConfigContainer   container;
    private ClientProvider    clientProvider;
    private DapsTokenProvider tokenProvider;

    /**
     * Creates the IDSBrokerCommunication controller.
     *
     * @param container     Configuration container
     * @param provider      providing underlying OkHttpClient
     * @param tokenProvider providing DAT Token for RequestMessage
     */
    public BrokerService( ConfigContainer container, ClientProvider provider, DapsTokenProvider tokenProvider ) {
        this.container = container;
        this.clientProvider = provider;
        this.tokenProvider = tokenProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response removeResourceFromBroker( String brokerURI, Resource resource )
            throws IOException, DapsTokenManagerException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder
                .buildResourceUnavailableMessage(securityToken, INFO_MODEL_VERSION, connectorID, resource);
        var payload = serializeResource(resource);

        var body = getMessageBody(header, payload);

        logSendingMessage(brokerURI);
        return sendBrokerMessage(brokerURI, body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response updateResourceAtBroker( String brokerURI, Resource resource ) throws
            IOException,
            DapsTokenManagerException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder
                .buildResourceUpdateMessage(securityToken, INFO_MODEL_VERSION, connectorID, resource);
        var payload = serializeResource(resource);

        var body = getMessageBody(header, payload);

        logSendingMessage(brokerURI);
        return sendBrokerMessage(brokerURI, body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response unregisterAtBroker( String brokerURI ) throws IOException, DapsTokenManagerException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder.buildUnavailableMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        var payload = IdsMessageUtils.buildSelfDeclaration(container.getConnector());

        var body = getMessageBody(header, payload);

        logSendingMessage(brokerURI);
        return sendBrokerMessage(brokerURI, body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response updateSelfDescriptionAtBroker( String brokerURI ) throws
            IOException, DapsTokenManagerException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder.buildUpdateMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        var payload = IdsMessageUtils.buildSelfDeclaration(container.getConnector());

        var body = getMessageBody(header, payload);

        logSendingMessage(brokerURI);
        return sendBrokerMessage(brokerURI, body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Response> updateSelfDescriptionAtBrokers( List<String> brokerUris ) throws
            IOException, DapsTokenManagerException {
        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder.buildUpdateMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        var payload = IdsMessageUtils.buildSelfDeclaration(container.getConnector());

        var body = getMessageBody(header, payload);

        var result = new ArrayList<Response>();
        for( var uri : brokerUris ) {
            logSendingMessage(uri);

            clientProvider.getClient().newCall(new Request.Builder().url(uri).post(body).build()).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure( @NotNull Call call, @NotNull IOException e ) {
                            log.warn(String.format("Connection to Broker %s failed!", uri));
                            log.warn(e.getMessage(), e);
                        }

                        @Override
                        public void onResponse( @NotNull Call call, @NotNull Response response ) {
                            log.info(String.format("Received response from %s", uri));
                            result.add(response);
                        }
                    }
            );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response queryBroker( String brokerURI, String query, QueryLanguage queryLanguage, QueryScope queryScope,
                                 QueryTarget queryTarget ) throws IOException, DapsTokenManagerException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder
                .buildQueryMessage(securityToken, INFO_MODEL_VERSION, connectorID, queryLanguage, queryScope,
                                   queryTarget);

        var body = getMessageBody(header, query);

        logSendingMessage(brokerURI);
        return sendBrokerMessage(brokerURI, body);
    }

    /**
     * Send the given RequestBody to the broker at the given URI and return the response
     *
     * @param brokerURI   URI of the Broker the Message is sent to
     * @param requestBody requestBody that is sent
     *
     * @return Response from the broker
     *
     * @throws IOException if requestBody cannot be sent
     */
    private Response sendBrokerMessage( String brokerURI, RequestBody requestBody ) throws IOException {
        var response = clientProvider.getClient().newCall(
                new Request.Builder().url(brokerURI).post(requestBody).build()
        ).execute();

        if( !response.isSuccessful() ) {
            log.warn("Response of the Broker wasn't successful!");
        }

        return response;
    }

    @NotNull
    private URI getConnectorId() {
        return container.getConnector().getId();
    }

    private DynamicAttributeToken getDat()
            throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        return tokenProvider.getDAT();
    }

    private MultipartBody getMessageBody( String header, String payload ) {
        return MessageBuilder.buildRequestBody(header, payload);
    }

    private void logBuildingHeader() {
        log.debug("Building message header");
    }

    private void logSendingMessage( String brokerURI ) {
        log.debug(String.format("Sending message to %s", brokerURI));
    }

    @NotNull
    private String serializeResource( Resource resource ) throws IOException {
        return SERIALIZER.serialize(resource);
    }
}
