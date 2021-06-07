package de.fraunhofer.ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.MultipartRequestBuilder;
import de.fraunhofer.ids.messaging.protocol.multipart.MultipartResponseConverter;
import de.fraunhofer.ids.messaging.util.RequestUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Option for the connector developer to choose the protocol for sending the message in the IDS dynamically per message.
 * Additionally a default if no protocol is specified.
 */
@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MessageService {

    IdsHttpService             httpService;
    MultipartRequestBuilder    multipartRequestBuilder    = new MultipartRequestBuilder();
    MultipartResponseConverter multipartResponseConverter = new MultipartResponseConverter();
    Serializer                 serializer                 = new Serializer();

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
     * @param messageAndPayload The IDS Infomodel Message containing the Metadata, and the Payload to be sent
     * @param target            The target of the message
     * @param protocolType      The selected protocol which should be used for sending (see ProtocolType enum)
     * @return returns the response
     * @throws MultipartParseException something went wrong with the file attached (if there was one)
     * @throws ClaimsException         something went wrong with the DAT
     * @throws IOException             DAPS or target could not be reached
     */
    public MessageAndPayload<?, ?> sendIdsMessage(final MessageAndPayload<?, ?> messageAndPayload,
                                                  final URI target,
                                                  final ProtocolType protocolType)
            throws MultipartParseException, ClaimsException, IOException {

        final var payloadOptional = messageAndPayload.getPayload();
        var payloadString = "";

        if (payloadOptional.isPresent()) {
            final var payload = payloadOptional.get();

            if (!(payload instanceof String)) {
                payloadString = serializer.serialize(payload);
            } else {
                payloadString = (String) payload;
            }

        }

        switch (protocolType) {
            case REST:
                return null;
            case MULTIPART:
                final var request = multipartRequestBuilder.build(messageAndPayload.getMessage(), target, payloadString);

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
     * Send messages in IDS to other actors without choosing a specific protocol, will use Multipart as default.
     *
     * @param messageAndPayload The IDS Infomodel Message containing the Metadata, and the Payload to be sent
     * @param target            The target of the message
     *
     * @return returns the response
     *
     * @throws MultipartParseException something went wrong with the file attached (if there was one)
     * @throws ClaimsException         something went wrong with the DAT
     * @throws IOException             DAPS or target could not be reached
     */
    public MessageAndPayload<?, ?> sendIdsMessage(final MessageAndPayload<?, ?> messageAndPayload, final URI target)
            throws MultipartParseException, ClaimsException, IOException {
        return sendIdsMessage(messageAndPayload, target, ProtocolType.MULTIPART);
    }
}
