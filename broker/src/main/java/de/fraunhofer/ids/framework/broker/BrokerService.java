package de.fraunhofer.ids.framework.broker;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.framework.daps.DapsConnectionException;
import de.fraunhofer.ids.framework.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.framework.daps.DapsTokenManagerException;
import de.fraunhofer.ids.framework.daps.DapsTokenProvider;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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
    public BrokerService( final ConfigContainer container,
                          final ClientProvider provider,
                          final DapsTokenProvider tokenProvider ) {
        this.container = container;
        this.clientProvider = provider;
        this.tokenProvider = tokenProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response removeResourceFromBroker( final String brokerURI, final Resource resource )
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
    public Response updateResourceAtBroker( final String brokerURI, final Resource resource ) throws
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
    public Response unregisterAtBroker( final String brokerURI ) throws IOException, DapsTokenManagerException {
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
    public Response updateSelfDescriptionAtBroker( final String brokerURI ) throws
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
    public List<Response> updateSelfDescriptionAtBrokers( final List<String> brokerUris ) throws
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
    public Response queryBroker( final String brokerURI,
                                 final String query,
                                 final QueryLanguage queryLanguage,
                                 final QueryScope queryScope,
                                 final QueryTarget queryTarget ) throws IOException, DapsTokenManagerException {
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
    private Response sendBrokerMessage( final String brokerURI, final RequestBody requestBody ) throws IOException {
        var response = clientProvider.getClient().newCall(
                new Request.Builder().url(brokerURI).post(requestBody).build()
        ).execute();

        if( !response.isSuccessful() ) {
            log.warn("Response of the Broker wasn't successful!");
        }

        return response;
    }

    /**
     * Get the ID of the connector
     *
     * @return Connector-ID URI
     */
    @NotNull
    private URI getConnectorId() {
        return container.getConnector().getId();
    }

    /**
     * Get a new DAT from the DAPS
     *
     * @return DAT, returned by the DAPS for the Connector
     *
     * @throws ConnectorMissingCertExtensionException Something went wrong with the Certificate of the Connector
     * @throws DapsConnectionException                The DAPS is not reachable (wrong URL, network problems..)
     * @throws DapsEmptyResponseException             The DAPS didn't return the expected response (maybe DAPS internal Problem?)
     */
    private DynamicAttributeToken getDat()
            throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        return tokenProvider.getDAT();
    }

    /**
     * Build the MultiPart message with header and payload
     *
     * @param header  Header of the MultiPart message
     * @param payload Payload of the MultiPart message
     *
     * @return Generated MultiPart Message
     */
    private MultipartBody getMessageBody( final String header, final String payload ) {
        return MessageBuilder.buildRequestBody(header, payload);
    }

    /**
     * Log info about starting to build the header
     */
    private void logBuildingHeader() {
        log.debug("Building message header");
    }

    /**
     * Log info about starting to send the message to the Broker
     *
     * @param brokerURI URI of the Broker
     */
    private void logSendingMessage( final String brokerURI ) {
        log.debug(String.format("Sending message to %s", brokerURI));
    }

    /**
     * Serializes a given Resource
     *
     * @param resource The Resource to be serialized
     *
     * @return String, the serialized Resource
     *
     * @throws IOException Something went wrong reading the given Resource
     */
    @NotNull
    private String serializeResource( final Resource resource ) throws IOException {
        return SERIALIZER.serialize(resource);
    }
}
