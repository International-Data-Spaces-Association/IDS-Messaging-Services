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
import de.fraunhofer.ids.framework.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.MultipartRequestBuilder;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import org.apache.commons.fileupload.FileUploadException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import static de.fraunhofer.ids.framework.messaging.util.RequestUtils.logRequest;

/**
 * Broker Communication Controller. Generates appropriate ids multipart messages and sends them to the broker
 * infrastructure api.
 **/
@Slf4j
@Service
public class BrokerService implements IDSBrokerService {
    private static final String                  INFO_MODEL_VERSION        = "4.0.0";
    private static final Serializer              SERIALIZER                = new Serializer();
    private static final MultipartRequestBuilder MULTIPART_REQUEST_BUILDER = new MultipartRequestBuilder();


    private ConfigContainer   container;
    private ClientProvider    clientProvider;
    private DapsTokenProvider tokenProvider;
    private IdsHttpService    idsHttpService;

    /**
     * Creates the IDSBrokerCommunication controller.
     *
     * @param container      Configuration container
     * @param provider       providing underlying OkHttpClient
     * @param tokenProvider  providing DAT Token for RequestMessage
     * @param idsHttpService providing sending capabilities
     */
    public BrokerService( ConfigContainer container, ClientProvider provider, DapsTokenProvider tokenProvider,
                          IdsHttpService idsHttpService ) {
        this.container = container;
        this.clientProvider = provider;
        this.tokenProvider = tokenProvider;
        this.idsHttpService = idsHttpService;

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

        Request request = MULTIPART_REQUEST_BUILDER.build(header, URI.create(brokerURI), payload);
        logRequest(request);
        return idsHttpService.send(request);
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

        Request request = MULTIPART_REQUEST_BUILDER.build(header, URI.create(brokerURI), payload);
        logRequest(request);
        return idsHttpService.send(request);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Response unregisterAtBroker( String brokerURI )
            throws IOException, DapsTokenManagerException, ClaimsException, FileUploadException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder.buildUnavailableMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        var payload = IdsMessageUtils.buildSelfDeclaration(container.getConnector());

        Request request = MULTIPART_REQUEST_BUILDER.build(header, URI.create(brokerURI), payload);
        logRequest(request);
        return idsHttpService.send(request);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Response updateSelfDescriptionAtBroker( String brokerURI ) throws
            IOException, DapsTokenManagerException {
        logBuildingHeader();

        var securityToken = getDat();
        var connectorID = getConnectorId();

        var header = MessageBuilder.buildUpdateMessage(securityToken, INFO_MODEL_VERSION, connectorID);
        var payload = IdsMessageUtils.buildSelfDeclaration(container.getConnector());

        Request request = MULTIPART_REQUEST_BUILDER.build(header, URI.create(brokerURI), payload);
        logRequest(request);
        return idsHttpService.send(request);

    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public List<Response> updateSelfDescriptionAtBrokers( List<String> brokerURIs ) throws
            DapsTokenManagerException {
        ArrayList<Response> responses = new ArrayList<Response>();
        for( var uri : brokerURIs ) {
            try {
                Response response = updateSelfDescriptionAtBroker(uri);
                if( response.isSuccessful() ) {
                    log.info(String.format("Received response from %s", uri));
                    responses.add(response);
                } else {
                    log.warn(String.format("Connection to Broker %s failed!", uri));
                }
            } catch( IOException e ) {
                log.warn(String.format("Connection to Broker %s failed!", uri));
                log.warn(e.getMessage(), e);
            }
        }
        return responses;
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

        Request request = MULTIPART_REQUEST_BUILDER.build(header, URI.create(brokerURI));
        logRequest(request);
        return idsHttpService.send(request);
    }

    /**
     * Send the given RequestBody to the broker at the given URI and return the response
     * @deprecated is replaced by the functionality of {@link IdsHttpService}
     * @param brokerURI   URI of the Broker the Message is sent to
     * @param requestBody requestBody that is sent
     *
     * @return Response from the broker
     *
     * @throws IOException if requestBody cannot be sent
     */
    @Deprecated( forRemoval = true )
    private Response sendBrokerMessage( String brokerURI, RequestBody requestBody ) throws IOException {
        Request request = new Request.Builder().url(brokerURI).post(requestBody).build();
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            System.out.println(buffer.readUtf8());
        } catch( final IOException e ) {
        }

        var response = clientProvider.getClient().newCall(request).execute();

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

    @Deprecated( forRemoval = true )
    private MultipartBody getMessageBody( String header, String payload ) {
        return MessageBuilder.buildRequestBody(header, payload);
    }

    private void logBuildingHeader() {
        log.debug("Building message header");
    }

    @Deprecated( forRemoval = true )
    private void logSendingMessage( String brokerURI ) {
        log.debug(String.format("Sending message to %s", brokerURI));
    }

    @NotNull
    private String serializeResource( Resource resource ) throws IOException {
        return SERIALIZER.serialize(resource);
    }
}
