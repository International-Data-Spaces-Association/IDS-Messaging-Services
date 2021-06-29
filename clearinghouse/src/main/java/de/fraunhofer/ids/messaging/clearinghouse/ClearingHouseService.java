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
import de.fraunhofer.ids.messaging.common.MessageBuilderException;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.requests.IdsRequestBuilderService;
import de.fraunhofer.ids.messaging.requests.InfrastructureService;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.protocol.UnexpectedResponseException;
import de.fraunhofer.ids.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.MultipartResponseConverter;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
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
    MultipartResponseConverter multipartResponseConverter = new MultipartResponseConverter();

    IdsHttpService idsHttpService;

    @NonFinal
    @Value("${clearinghouse.url}")
    String clearingHouseUrl;

    @NonFinal
    @Value("${clearinghouse.query.endpoint:/messages/query}")
    String queryEndpoint;

    @NonFinal
    @Value("${clearinghouse.log.endpoint:/messages/log}")
    String logEndpoint;

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
            IOException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            ShaclValidatorException,
            SerializeException,
            MessageBuilderException {
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
            MultipartParseException,
            UnknownResponseException,
            DeserializeException,
            ShaclValidatorException,
            SerializeException,
            MessageBuilderException {

        //Build IDS Multipart Message
        final var body = buildMultipartWithInternalHeaders(
                MessageBuilder.buildLogMessage(container, tokenProvider, clearingHouseUrl),
                serializer.serialize(messageToLog),
                MediaType.parse("application/json"));

        //set some random id for message
        final var response = idsHttpService.sendAndCheckDat(body, new URI(clearingHouseUrl + logEndpoint + "/" + pid));
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
            MessageBuilderException {

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
                ? new URI(clearingHouseUrl + queryEndpoint)
                : messageId == null
                        ? new URI(String.format("%s/%s", clearingHouseUrl + queryEndpoint, pid))
                        : new URI(String.format("%s/%s/%s", clearingHouseUrl + queryEndpoint, pid, messageId));

        final var response = idsHttpService.sendAndCheckDat(body, targetURI);
        final var map = multipartResponseConverter.convertResponse(response);
        return expectMapOfTypeT(map, ResultMAP.class);

    }

    /**
     * @param headerMessage  IDS Message used as Header
     * @param payloadContent Payload String
     * @param payloadType    MediaType of Payload String
     *
     * @return built MultipartBody
     *
     * @throws SerializeException when headerMessage cannot be serialized
     */
    private MultipartBody buildMultipartWithInternalHeaders(final Message headerMessage,
                                                            final String payloadContent,
                                                            final MediaType payloadType)
            throws SerializeException {
        try {
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
            final var header =
                    MultipartBody.Part.create(headerHeader, headerBody);
            bodyBuilder.addPart(header);

            if (payloadContent != null && !payloadContent.isBlank()) {
                //Create Header for payload Part of IDS Multipart Message
                final var payloadHeader = new Headers.Builder()
                        .add("Content-Disposition: form-data; name=\"payload\"")
                        .build();

                //Create RequestBody for payload Part of IDS Multipart Message (with json content-type)
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
