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
package ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.protocol.http.IdsHttpService;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.MessageAndPayload;
import ids.messaging.protocol.multipart.MultipartRequestBuilder;
import ids.messaging.protocol.multipart.MultipartResponseConverter;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Option for the connector developer to choose the protocol for sending the message in the
 * IDS dynamically per message. Additionally a default if no protocol is specified.
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
    private final MultipartRequestBuilder multipartRequestBuilder = new MultipartRequestBuilder();

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
     * @param messageAndPayload The IDS Infomodel Message containing the Metadata, and the
     * Payload to be sent.
     * @param target The target of the message.
     * @param protocolType The selected protocol which should be used for sending
     * (see ProtocolType enum).
     * @return Returns the response.
     * @throws MultipartParseException If the content cannot be parsed.
     * @throws ClaimsException If the claims cannot be successfully verified.
     * @throws UnknownResponseException If the format of the answer is not known.
     * @throws DeserializeException If the deserialization of the received message fails.
     * @throws SerializeException If there are problems with serializing.
     * @throws IOException Other errors, which were not categorized.
     * @throws SendMessageException If there is an error when sending the request.
     * @throws ShaclValidatorException If the message does not pass the SHACL validation test.
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

                final var responseMap = httpService.sendAndCheckDat(request);

                return multipartResponseConverter.convertResponse(responseMap);
            default:
                return sendIdsMessage(messageAndPayload, target, ProtocolType.MULTIPART);
        }
    }

    /**
     * Send messages in IDS to other actors without choosing a specific protocol, will
     * use Multipart as default.
     *
     * @param messageAndPayload The IDS Infomodel Message containing the Metadata, and the
     *                          Payload to be sent.
     * @param target The target of the message.
     * @return Returns the response.
     * @throws MultipartParseException Something went wrong with the file attached
     * (if there was one).
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws IOException Other errors, which were not categorized.
     * @throws ShaclValidatorException If the message does not pass the SHACL validation test.
     * @throws SerializeException If there are problems with serializing.
     * @throws UnknownResponseException If the format of the answer is not known.
     * @throws SendMessageException If there is an error when sending the request.
     * @throws DeserializeException If the deserialization of the received message fails.
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
