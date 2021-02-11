package de.fraunhofer.ids.framework.clearinghouse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Map;

import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.DapsTokenManagerException;
import de.fraunhofer.ids.framework.daps.DapsTokenProvider;
import de.fraunhofer.ids.framework.messaging.protocol.http.HttpService;
import de.fraunhofer.ids.framework.messaging.util.InfomodelMessageBuilder;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils.getGregorianNow;

@Component
//TODO AISEC clearing house is not using multipart, modify when messaging supports multiple protocols
public class ClearingHouseClientImpl implements ClearingHouseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearingHouseClientImpl.class);
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

    @Override
    public Response sendLogToClearingHouse( String logMessage ) throws ClearingHouseClientException {
        try {
            var header = buildHeader();
            var body = InfomodelMessageBuilder.messageWithString(header, logMessage);
            return httpService.sendWithHeaders(body, new URI(clearingHouseUrl + Math.abs(secureRandom.nextInt())),
                                               Map.of("Authorization",
                                                      "Bearer " + dapsTokenProvider.provideDapsToken()));
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

    private LogMessage buildHeader() throws DapsTokenManagerException, URISyntaxException {
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
}
