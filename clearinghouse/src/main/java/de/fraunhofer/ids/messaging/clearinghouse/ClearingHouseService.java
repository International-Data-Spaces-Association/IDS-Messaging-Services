package de.fraunhofer.ids.messaging.clearinghouse;

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
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.core.util.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.InfrastructureService;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.messaging.protocol.multipart.MultipartResponseConverter;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ClearingHouseService extends InfrastructureService implements IDSClearingHouseService  {

    Serializer   serializer   = new Serializer();
    SecureRandom secureRandom = new SecureRandom();
    MultipartResponseConverter multipartResponseConverter= new MultipartResponseConverter();

    IdsHttpService idsHttpService;

    @NonFinal
    @Value("${clearinghouse.url}")
    private String clearingHouseUrl;

    public ClearingHouseService(final ConfigContainer container,
                                final DapsTokenProvider tokenProvider,
                                final MessageService messageService,
                                final IdsHttpService idsHttpService) {
        super(container, tokenProvider, messageService);
        this.idsHttpService = idsHttpService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProcessedNotificationMAP sendLogToClearingHouse(final Message messageToLog)
            throws DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            URISyntaxException,
            IOException {
        //log message under some random processId
        final var pid = Math.abs(secureRandom.nextInt());

        return sendLogToClearingHouse(messageToLog, String.valueOf(pid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProcessedNotificationMAP sendLogToClearingHouse(final Message messageToLog,
                                                                  final String pid)
            throws
            DapsTokenManagerException,
            URISyntaxException,
            IOException,
            ClaimsException,
            MultipartParseException {

        //Build IDS Multipart Message
        final var body = buildMultipartWithInternalHeaders(
                MessageBuilder.buildLogMessage(container, tokenProvider, clearingHouseUrl),
                serializer.serialize(messageToLog),
                MediaType.parse("application/json"));

        //set some random id for message
        final var response = idsHttpService.sendAndCheckDat(body, new URI(clearingHouseUrl + pid));
        final var map = multipartResponseConverter.convertResponse(response);
        return expectMessageProcessedNotificationMAP(map);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResultMAP queryClearingHouse(final String pid,
                                        final String messageId,
                                        final QueryLanguage queryLanguage,
                                        final QueryScope queryScope,
                                        final QueryTarget queryTarget,
                                        final String query)
            throws
            DapsTokenManagerException,
            URISyntaxException,
            ClaimsException,
            MultipartParseException,
            IOException {

        //Build IDS Multipart Message
        final var body = buildMultipartWithInternalHeaders(
                MessageBuilder.buildQueryMessage(
                        queryLanguage, queryScope, queryTarget, container,
                        tokenProvider),
                query,
                MediaType.parse("text/plain")
        );

        //build targetURI of QueryMessage (if pid and messageid are given)
        final var targetURI = (pid == null)
                ? new URI(clearingHouseUrl)
                : messageId == null
                        ? new URI(String.format("%s%s", clearingHouseUrl, pid))
                        : new URI(String.format("%s%s/%s", clearingHouseUrl, pid, messageId));

        final var response = idsHttpService.sendAndCheckDat(body, targetURI);
        final var map = multipartResponseConverter.convertResponse(response);
        return expectResultMAP(map);

    }

    /**
     * @param headerMessage  IDS Message used as Header
     * @param payloadContent Payload String
     * @param payloadType    MediaType of Payload String
     *
     * @return built MultipartBody
     *
     * @throws IOException when headerMessage cannot be serialized
     */
    private MultipartBody buildMultipartWithInternalHeaders(final Message headerMessage,
                                                            final String payloadContent,
                                                            final MediaType payloadType)
            throws IOException {

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

        if (payloadContent != null && !payloadContent.isBlank())  {
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
