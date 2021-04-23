package de.fraunhofer.ids.framework.clearinghouse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Objects;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.DapsTokenManagerException;
import de.fraunhofer.ids.framework.daps.DapsTokenProvider;
import de.fraunhofer.ids.framework.messaging.protocol.http.HttpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClearingHouseService implements IDSClearingHouseService {

    private final Serializer   serializer   = new Serializer();
    private final SecureRandom secureRandom = new SecureRandom();

    private final ConfigContainer   configContainer;
    private final DapsTokenProvider dapsTokenProvider;
    private final HttpService       httpService;

    @Value("${clearinghouse.url}")
    private String clearingHouseUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    public Response sendLogToClearingHouse(final Message messageToLog) throws ClearingHouseClientException {
        //log message under some random processId
        final var id = Math.abs(secureRandom.nextInt());

        return sendLogToClearingHouse(messageToLog, String.valueOf(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response sendLogToClearingHouse(final Message messageToLog, final String pid)
            throws ClearingHouseClientException {
        try {
            //Build IDS Multipart Message
            final var body = buildMultipartWithInternalHeaders(
                    MessageBuilder.buildLogMessage(configContainer, dapsTokenProvider, clearingHouseUrl),
                    serializer.serialize(messageToLog),
                    MediaType.parse("application/json"));

            //set some random id for message
            return httpService.send(body, new URI(clearingHouseUrl + pid));
        } catch (DapsTokenManagerException e) {
            return throwClearingHouseException(e, "Could not get a DAT for sending the LogMessage!");
        } catch (URISyntaxException e) {
            return throwClearingHouseException(e, String.format("Clearing House URI could not be parsed from String: %s!", clearingHouseUrl));
        } catch (IOException e) {
            return throwClearingHouseException(e, "Error while serializing LogMessage header or sending the request!");
        }
    }

    private Response throwClearingHouseException(final Exception e, final String message) throws ClearingHouseClientException {
        if (log.isWarnEnabled()) {
            log.warn(e.getMessage(), e);
        }
        throw new ClearingHouseClientException(message, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response queryClearingHouse(final String pid, final String messageid, final QueryLanguage queryLanguage,
                                       final QueryScope queryScope, final QueryTarget queryTarget, final String query)
            throws ClearingHouseClientException {
        try {
            //Build IDS Multipart Message
            final var body = buildMultipartWithInternalHeaders(
                    MessageBuilder.buildQueryMessage(
                            queryLanguage, queryScope, queryTarget, configContainer,
                            dapsTokenProvider),
                    query,
                    MediaType.parse("text/plain")
            );

            //build targetURI of QueryMessage (if pid and messageid are given)
            final var targetURI = (pid == null) ?
                    new URI(clearingHouseUrl) :
                    (messageid == null ?
                            new URI(String.format("%s%s", clearingHouseUrl, pid)) :
                            new URI(String.format("%s%s/%s", clearingHouseUrl, pid, messageid)
                            )
                    );

            return httpService.send(body, targetURI);
        } catch (DapsTokenManagerException e) {
            return throwClearingHouseException(e, "Could not get a DAT for sending the LogMessage!");
        } catch (URISyntaxException e) {
            return throwClearingHouseException(e, String.format("Clearing House URI could not be parsed from String: %s!", clearingHouseUrl));
        } catch (IOException e) {
            return throwClearingHouseException(e, "Error while serializing LogMessage header or sending the request!");
        }
    }

    /**
     * @param headerMessage  IDS Message used as Header
     * @param payloadContent Payload String
     * @param payloadType    MediaType of Payload String
     * @return built MultipartBody
     * @throws IOException when headerMessage cannot be serialized
     */
    private MultipartBody buildMultipartWithInternalHeaders(final Message headerMessage,
                                                            final String payloadContent,
                                                            final MediaType payloadType) throws IOException {
        final var bodyBuilder = new MultipartBody.Builder();

        //OkHttp does not support setting Content Type on Multipart Parts directly on creation, workaround
        //Create Header for header Part of IDS Multipart Message
        final var headerHeader = new Headers.Builder()
                .add("Content-Disposition: form-data; name=\"header\"")
                .build();

        //Create RequestBody for header Part of IDS Multipart Message (with json content-type)
        final var headerBody = RequestBody.create(
                serializer.serialize(headerMessage),
                MediaType.parse("application/json+ld"));

        //Create header Part of Multipart Message
        final var header = MultipartBody.Part.create(headerHeader, headerBody);
        bodyBuilder.addPart(header);

        if (payloadContent != null && !payloadContent.isBlank()) {
            //Create Header for payload Part of IDS Multipart Message
            final var payloadHeader = new Headers.Builder()
                    .add("Content-Disposition: form-data; name=\"payload\"")
                    .build();

            //Create RequestBody for payload Part of IDS Multipart Message (with json content-type)
            final var payloadBody = RequestBody.create(payloadContent, payloadType);

            //Create payload Part of Multipart Message
            final var payload = MultipartBody.Part.create(payloadHeader, payloadBody);
            bodyBuilder.addPart(payload);
        }
        //Build IDS Multipart Message
        return bodyBuilder.setType(
                Objects.requireNonNull(
                        MediaType.parse("multipart/form-data")
                )).build();
    }
}
