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
package ids.messaging.clearinghouse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.UnexpectedResponseException;
import ids.messaging.protocol.http.IdsHttpService;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.MultipartResponseConverter;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import ids.messaging.protocol.multipart.mapping.ResultMAP;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.InfrastructureService;
import ids.messaging.requests.NotificationTemplateProvider;
import ids.messaging.requests.RequestTemplateProvider;
import ids.messaging.requests.builder.IdsRequestBuilderService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Service class for communication to the ClearingHouse.
 */
@Slf4j
@Component
public class ClearingHouseService extends InfrastructureService
        implements IDSClearingHouseService {

    /**
     * The infomodel serializer.
     */
    private final Serializer   serializer   = new Serializer();

    /**
     * The MultipartResponseConverter.
     */
    private final MultipartResponseConverter multipartResponseConverter
            = new MultipartResponseConverter();

    /**
     * The IdsHttpService.
     */
    private final IdsHttpService idsHttpService;

    /**
     * The NotificationTemplateProvider.
     */
    private final NotificationTemplateProvider notificationTemplateProvider;

    /**
     * The RequestTemplateProvider.
     */
    private final RequestTemplateProvider requestTemplateProvider;

    /**
     * The base URL of the CH.
     */
    @Value("${clearinghouse.url}")
    private String clearingHouseUrl;

    /**
     * The CH endpoint for query messages.
     */
    @Value("${clearinghouse.query.endpoint:/messages/query}")
    private String queryEndpoint;

    /**
     * The CH endpoint for logging.
     */
    @Value("${clearinghouse.log.endpoint:/messages/log}")
    private String logEndpoint;

    /**
     * The CH endpoint for creating PIDs.
     */
    @Value("${clearinghouse.process.endpoint:/process}")
    private String processEndpoint;

    /**
     * Constructor for the ClearingHouseService.
     *
     * @param container The ConfigContainer.
     * @param tokenProvider The DapsTokenProvider.
     * @param messageService The MessageService.
     * @param idsHttpService The IdsHttpService.
     * @param notificationTemplateProvider The NotificationTemplateProvider.
     * @param requestTemplateProvider The RequestTemplateProvider.
     * @param idsRequestBuilderService The IdsRequestBuilderService.
     */
    public ClearingHouseService(final ConfigContainer container,
                                final DapsTokenProvider tokenProvider,
                                final MessageService messageService,
                                final IdsHttpService idsHttpService,
                                final NotificationTemplateProvider notificationTemplateProvider,
                                final RequestTemplateProvider requestTemplateProvider,
                                final IdsRequestBuilderService idsRequestBuilderService) {
        super(container, tokenProvider, messageService, idsRequestBuilderService);
        this.idsHttpService = idsHttpService;
        this.notificationTemplateProvider = notificationTemplateProvider;
        this.requestTemplateProvider = requestTemplateProvider;
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
            MultipartParseException,
            UnknownResponseException,
            DeserializeException,
            ShaclValidatorException,
            SerializeException,
            UnexpectedResponseException {

        //Build IDS Multipart Message
        final var body = buildMultipartWithInternalHeaders(
                notificationTemplateProvider
                    .logMessageTemplate(new URI(clearingHouseUrl)).buildMessage(),
                serializer.serialize(messageToLog),
                MediaType.parse("application/json"));

        //set given id for message
        final var response = idsHttpService
            .sendAndCheckDat(body, new URI(clearingHouseUrl + logEndpoint + "/" + pid));
        final var map = multipartResponseConverter.convertResponse(response);
        return expectMapOfTypeT(map, MessageProcessedNotificationMAP.class);
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
            IOException,
            UnknownResponseException,
            DeserializeException,
            ShaclValidatorException,
            SerializeException,
            UnexpectedResponseException {

        //Build IDS Multipart Message
        final var body = buildMultipartWithInternalHeaders(
                requestTemplateProvider
                        .queryMessageTemplate(queryLanguage,
                                              queryScope,
                                              queryTarget).buildMessage(),
                query,
                MediaType.parse("text/plain")
        );

        //build targetURI of QueryMessage (if pid and messageid are given)
        final var targetURI = (pid == null)
            ? new URI(clearingHouseUrl + queryEndpoint)
            : messageId == null
                ? new URI(String.format("%s/%s",
                                            clearingHouseUrl + queryEndpoint,
                                            pid))
                : new URI(String.format("%s/%s/%s",
                                            clearingHouseUrl + queryEndpoint,
                                            pid,
                                            messageId));

        final var response = idsHttpService.sendAndCheckDat(body, targetURI);
        final var map = multipartResponseConverter.convertResponse(response);
        return expectMapOfTypeT(map, ResultMAP.class);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProcessedNotificationMAP registerPidAtClearingHouse(final String pid,
                                                                      final String... connectorIDs)
            throws DapsTokenManagerException,
            URISyntaxException,
            ClaimsException,
            MultipartParseException,
            IOException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            ShaclValidatorException,
            SerializeException {
        //Build request json
        final var payload = new JSONObject();
        payload.put("owners", new JSONArray(connectorIDs));

        //Build IDS Multipart Message
        final var body = buildMultipartWithInternalHeaders(
                requestTemplateProvider
                        .requestMessageTemplate().buildMessage(),
                payload.toString(),
                MediaType.parse("application/json"));

        //send message to clearinghouse
        final var response = idsHttpService
                .sendAndCheckDat(body, new URI(clearingHouseUrl + processEndpoint + "/" + pid));
        final var map = multipartResponseConverter.convertResponse(response);

        return expectMapOfTypeT(map, MessageProcessedNotificationMAP.class);
    }

    /**
     * @param headerMessage  IDS Message used as Header
     * @param payloadContent Payload String
     * @param payloadType    MediaType of Payload String
     * @return built MultipartBody
     * @throws SerializeException when headerMessage cannot be serialized
     */
    private MultipartBody buildMultipartWithInternalHeaders(final Message headerMessage,
                                                            final String payloadContent,
                                                            final MediaType payloadType)
            throws SerializeException {
        try {
            final var bodyBuilder = new MultipartBody.Builder();

            //OkHttp does not support setting Content Type on
            //Multipart Parts directly on creation, workaround
            //Create Header for header Part of IDS Multipart Message
            final var headerHeader = new Headers.Builder()
                    .add("Content-Disposition: form-data; name=\"header\"")
                    .build();

            //Create RequestBody for header Part of IDS Multipart Message (with json content-type)
            final var headerBody = RequestBody.create(
                    serializer.serialize(headerMessage),
                    MediaType.parse("application/json+ld"));

            //Create header Part of Multipart Message
            final var header =
                    MultipartBody.Part.create(headerHeader, headerBody);
            bodyBuilder.addPart(header);

            if (payloadContent != null && !payloadContent.isBlank()) {
                //Create Header for payload Part of IDS Multipart Message
                final var payloadHeader = new Headers.Builder()
                        .add("Content-Disposition: form-data; name=\"payload\"")
                        .build();

                //Create RequestBody for payload Part of IDS Multipart
                // Message (with json content-type)
                final var payloadBody =
                        RequestBody.create(payloadContent, payloadType);

                //Create payload Part of Multipart Message
                final var payload =
                        MultipartBody.Part.create(payloadHeader, payloadBody);
                bodyBuilder.addPart(payload);
            }
            //Build IDS Multipart Message
            return bodyBuilder.setType(
                    Objects.requireNonNull(
                            MediaType.parse("multipart/form-data")
                    )).build();
        } catch (IOException ioException) {
            throw new SerializeException(ioException);
        }
    }
}
