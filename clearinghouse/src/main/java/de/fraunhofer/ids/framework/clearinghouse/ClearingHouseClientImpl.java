package de.fraunhofer.ids.framework.clearinghouse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.*;
import de.fraunhofer.ids.framework.messaging.protocol.http.HttpService;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
import okhttp3.*;
import okhttp3.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils.getGregorianNow;

@Component
//TODO AISEC clearing house is not using multipart, modify when messaging supports multiple protocols
public class ClearingHouseClientImpl implements ClearingHouseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearingHouseClientImpl.class);
    private static final Serializer SERIALIZER = new Serializer();

    private final ClientProvider    clientProvider;
    private final ConfigContainer   configContainer;
    private final DapsTokenProvider dapsTokenProvider;
    private final HttpService       httpService;
    private final SecureRandom      secureRandom;

    @Value( "${clearinghouse.url}" )
    private String clearingHouseUrl;

    public ClearingHouseClientImpl( ClientProvider clientProvider, ConfigContainer configContainer,
                                    DapsTokenProvider dapsTokenProvider, HttpService httpService ) {
        this.clientProvider = clientProvider;
        this.configContainer = configContainer;
        this.dapsTokenProvider = dapsTokenProvider;
        this.httpService = httpService;
        this.secureRandom = new SecureRandom();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response sendLogToClearingHouse( Message messageToLog ) throws ClearingHouseClientException {
        //log message under some random processId
        var id = Math.abs(secureRandom.nextInt());
        return sendLogToClearingHouse(messageToLog, String.valueOf(id));
    }

    @Override
    public Response sendLogToClearingHouse(Message messageToLog, String pid) throws ClearingHouseClientException {
        try {
            //Build IDS Multipart Message
            var body = buildMultipartWithInternalHeaders(buildLogMessage(), SERIALIZER.serialize(messageToLog), MediaType.parse("application/json"));
            //set some random id for message
            return httpService.send(body, new URI(clearingHouseUrl + pid));
        } catch( DapsTokenManagerException e ) {
            LOGGER.warn(e.getMessage(), e);
            throw new ClearingHouseClientException("Could not get a DAT for sending the LogMessage!", e);
        } catch( URISyntaxException e ) {
            LOGGER.warn(e.getMessage(), e);
            throw new ClearingHouseClientException(
                    String.format("Clearing House URI could not be parsed from String: %s!", clearingHouseUrl), e);
        } catch( IOException e ) {
            LOGGER.warn(e.getMessage(), e);
            throw new ClearingHouseClientException("Error while serializing LogMessage header or sending the request!",
                    e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response queryClearingHouse(String pid, String messageid, QueryLanguage queryLanguage, QueryScope queryScope, QueryTarget queryTarget, String query) throws ClearingHouseClientException {
        //TODO QueryMessages provoke HTTP 500 at clearing house
        try {
            //Build IDS Multipart Message
            var body = buildMultipartWithInternalHeaders(
                    buildQueryMessage(queryLanguage, queryScope, queryTarget),
                    query,
                    MediaType.parse("text/plain")
            );
            //build targetURI of QueryMessage (if pid and messageid are given)
            URI targetURI;
            if(pid == null){
                targetURI = new URI(clearingHouseUrl);
            }else if(messageid == null){
                targetURI = new URI(String.format("%s%s", clearingHouseUrl, pid));
            }else{
                targetURI = new URI(String.format("%s%s/%s", clearingHouseUrl, pid, messageid));
            }
            return httpService.send(body, targetURI);
        } catch( DapsTokenManagerException e ) {
            LOGGER.warn(e.getMessage(), e);
            throw new ClearingHouseClientException("Could not get a DAT for sending the LogMessage!", e);
        } catch( URISyntaxException e ) {
            LOGGER.warn(e.getMessage(), e);
            throw new ClearingHouseClientException(
                    String.format("Clearing House URI could not be parsed from String: %s!", clearingHouseUrl), e);
        } catch( IOException e ) {
            LOGGER.warn(e.getMessage(), e);
            throw new ClearingHouseClientException("Error while serializing LogMessage header or sending the request!",
                    e);
        }
    }

    /**
     * @return a LogMessage to be used as Header
     * @throws DapsTokenManagerException when {@link DapsTokenProvider} cannot get a Token
     * @throws URISyntaxException when clearinghouse.url cannot be parsed as URI
     */
    private LogMessage buildLogMessage() throws DapsTokenManagerException, URISyntaxException {
        var connector = configContainer.getConnector();
        return new LogMessageBuilder()
                ._issued_(getGregorianNow())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._senderAgent_(connector.getId())
                ._securityToken_(dapsTokenProvider.getDAT())
                ._recipientConnector_(Util.asList(new URI(clearingHouseUrl)))
                .build();
    }

    /**
     * @param queryLanguage Language of the Query
     * @param queryScope Scope of the Query
     * @param queryTarget Target of the Query
     * @return built QueryMessage
     * @throws DapsTokenManagerException when {@link DapsTokenProvider} cannot get a Token
     */
    private QueryMessage buildQueryMessage(QueryLanguage queryLanguage, QueryScope queryScope, QueryTarget queryTarget) throws DapsTokenManagerException {
        var connector = configContainer.getConnector();
        return new QueryMessageBuilder()
                ._securityToken_(dapsTokenProvider.getDAT())
                ._issued_(getGregorianNow())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._senderAgent_(connector.getId())
                ._queryLanguage_(queryLanguage)
                ._queryScope_(queryScope)
                ._recipientScope_(queryTarget)
                .build();
    }

    /**
     * @param headerMessage IDS Message used as Header
     * @param payloadContent Payload String
     * @param payloadType MediaType of Payload String
     * @return built MultipartBody
     * @throws IOException when headerMessage cannot be serialized
     */
    private MultipartBody buildMultipartWithInternalHeaders(Message headerMessage, String payloadContent, MediaType payloadType) throws IOException {
        //OkHttp does not support setting Content Type on Multipart Parts directly on creation, workaround
        //Create Header for header Part of IDS Multipart Message
        var headerHeader = new Headers.Builder()
                .add("Content-Disposition: form-data; name=\"header\"")
                .build();
        //Create RequestBody for header Part of IDS Multipart Message (with json content-type)
        var headerBody = RequestBody.create(SERIALIZER.serialize(headerMessage), MediaType.parse("application/json"));
        //Create header Part of Multipart Message
        var header = MultipartBody.Part.create(headerHeader, headerBody);

        //Create Header for payload Part of IDS Multipart Message
        var payloadHeader = new Headers.Builder()
                .add("Content-Disposition: form-data; name=\"payload\"")
                .build();
        //Create RequestBody for payload Part of IDS Multipart Message (with json content-type)
        var payloadBody = RequestBody.create(payloadContent, payloadType);
        //Create payload Part of Multipart Message
        var payload = MultipartBody.Part.create(payloadHeader, payloadBody);
        //Build IDS Multipart Message
        return new MultipartBody.Builder().addPart(header).addPart(payload).build();
    }
}
