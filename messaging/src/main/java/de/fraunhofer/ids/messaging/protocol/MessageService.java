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
package de.fraunhofer.ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.MultipartRequestBuilder;
import de.fraunhofer.ids.messaging.protocol.multipart.MultipartResponseConverter;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Option for the connector developer to choose the protocol for
 * sending the message in the IDS dynamically per message.
 * Additionally a default if no protocol is specified.
 */
@Slf4j
@Service
public class MessageService {

    /**
     * The IdsHttpService.
     */
    private final IdsHttpService httpService;

    /**
     * The MultipartRequestBuilder.
     */
    private final MultipartRequestBuilder multipartRequestBuilder
                            = new MultipartRequestBuilder();

    /**
     * The MultipartResponseConverter.
     */
    private final MultipartResponseConverter multipartResponseConverter
                            = new MultipartResponseConverter();

    /**
     * The infomodel serializer.
     */
    private final Serializer serializer = new Serializer();

    /**
     * Constructor of MessageService class.
     *
     * @param httpService the IdsHttpService
     */
    @Autowired
    public MessageService(final IdsHttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * Send messages in IDS to other actors with choice of the protocol used.
     *
     * @param messageAndPayload The IDS Infomodel Message
     *                          containing the Metadata,
     *                          and the Payload to be sent
     * @param target The target of the message
     * @param protocolType The selected protocol which should
     *                     be used for sending (see ProtocolType enum)
     * @return returns the response
     * @throws MultipartParseException something went wrong with
     * the file attached (if there was one)
     * @throws ClaimsException something went wrong with the DAT
     * @throws IOException  DAPS or target could not be reached
     */
    public MessageAndPayload<?, ?> sendIdsMessage(
            final MessageAndPayload<?, ?> messageAndPayload,
            final URI target,
            final ProtocolType protocolType)
            throws
            MultipartParseException,
            ClaimsException,
            UnknownResponseException,
            DeserializeException,
            SerializeException,
            IOException,
            SendMessageException,
            ShaclValidatorException {

        final var payloadOptional = messageAndPayload.getPayload();
        var payloadString = "";

        if (payloadOptional.isPresent()) {
            final var payload = payloadOptional.get();

            if (!(payload instanceof String)) {
                try {
                    payloadString = serializer.serialize(payload);
                } catch (IOException ioException) {
                    //Map Serializer-IOException to SerializeException
                    throw new SerializeException(ioException);
                }
            } else {
                payloadString = (String) payload;
            }

        }

        switch (protocolType) {
            case REST:
                return null;
            case MULTIPART:
                final var request = multipartRequestBuilder
                        .build(messageAndPayload.getMessage(),
                               target,
                               payloadString);

                RequestUtils.logRequest(request);

                final var responseMap = httpService.sendAndCheckDat(request);

                return multipartResponseConverter.convertResponse(responseMap);
            default:
                if (log.isWarnEnabled()) {
                    log.warn("Unknown protocol using default multipart");
                }

                return sendIdsMessage(messageAndPayload, target, ProtocolType.MULTIPART);

        }
    }

    /**
     * Send messages in IDS to other actors without choosing a
     * specific protocol, will use Multipart as default.
     *
     * @param messageAndPayload The IDS Infomodel Message containing
     *                          the Metadata, and the Payload to be sent
     * @param target The target of the message
     *
     * @return returns the response
     *
     * @throws MultipartParseException something went wrong with the
     * file attached (if there was one)
     * @throws ClaimsException something went wrong with the DAT
     * @throws IOException DAPS or target could not be reached
     */
    public MessageAndPayload<?, ?> sendIdsMessage(
            final MessageAndPayload<?, ?> messageAndPayload, final URI target)
            throws
            MultipartParseException,
            ClaimsException,
            IOException,
            UnknownResponseException,
            DeserializeException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {
        return sendIdsMessage(messageAndPayload,
                              target,
                              ProtocolType.MULTIPART);
    }
}
