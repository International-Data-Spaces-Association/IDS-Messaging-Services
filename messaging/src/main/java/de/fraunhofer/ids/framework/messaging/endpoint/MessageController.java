package de.fraunhofer.ids.framework.messaging.endpoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.messaging.dispatcher.MessageDispatcher;
import de.fraunhofer.ids.framework.messaging.dispatcher.filter.PreDispatchingFilterException;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
import de.fraunhofer.ids.framework.util.MultipartDatapart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * REST controller for handling all incoming IDS multipart Messages.
 */
@Slf4j
@Controller
public class MessageController {

    private final MessageDispatcher messageDispatcher;
    private final ConfigContainer   configContainer;
    private final Serializer        serializer;

    @Autowired
    public MessageController(final MessageDispatcher messageDispatcher,
                             final Serializer serializer,
                             final ConfigContainer configContainer) {
        this.messageDispatcher = messageDispatcher;
        this.serializer = serializer;
        this.configContainer = configContainer;
    }

    /**
     * Generic method to handle all incoming ids messages. One Method to Rule them All.
     * Get header and payload from incoming message, let the MessageDispatcher and MessageHandler process it
     * and return the result as a Multipart response.
     *
     * @param request incoming http request
     * @return multipart MultivalueMap containing ResponseMessage header and some payload
     */
    public ResponseEntity<MultiValueMap<String, Object>> handleIDSMessage(final HttpServletRequest request) {
        try {
            final var headerPart = request.getPart(MultipartDatapart.HEADER.toString());
            final var payloadPart = request.getPart(MultipartDatapart.PAYLOAD.toString());

            if (headerPart == null) {
                if (log.isDebugEnabled()) {
                    log.debug("header of incoming message were empty!");
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(createDefaultErrorMessage(RejectionReason.MALFORMED_MESSAGE,
                                                                     "Header was missing!"));
            }

            String input;

            if (log.isDebugEnabled()) {
                log.debug("parsing header of incoming message");
            }

            try (var scanner = new Scanner(headerPart.getInputStream(), StandardCharsets.UTF_8.name())) {
                input = scanner.useDelimiter("\\A").next();
            }

            // Deserialize JSON-LD headerPart to its RequestMessage.class
            final var requestHeader = serializer.deserialize(input, Message.class);

            if (log.isDebugEnabled()) {
                log.debug("hand the incoming message to the message dispatcher!");
            }

            final var response = this.messageDispatcher.process(requestHeader,
                                                                payloadPart == null ? null : payloadPart
                                                                        .getInputStream()); //pass null if payloadPart is null, else pass it as inputStream

            if (response != null) {
                //get Response as MultiValueMap
                final var responseAsMap = createMultiValueMap(response.createMultipartMap(serializer));

                // return the ResponseEntity as Multipart content with created MultiValueMap
                if (log.isDebugEnabled()) {
                    log.debug("sending response with status OK (200)");
                }

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(responseAsMap);
            } else {
                //if no response-body specified by the implemented handler of the connector (e.g. for received RequestInProcessMessage)

                if (log.isWarnEnabled()) {
                    log.warn("Implemented Message-Handler didn't return a response!");
                }

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .build();
            }
        } catch (PreDispatchingFilterException e) {
            if (log.isErrorEnabled()) {
                log.error("Error during pre-processing with a PreDispatchingFilter!", e);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(createDefaultErrorMessage(RejectionReason.BAD_PARAMETERS,
                                                                 String.format("Error during preprocessing: %s",
                                                                               e.getMessage())));
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("incoming message could not be parsed!");
                log.warn(e.getMessage(), e);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(createDefaultErrorMessage(RejectionReason.MALFORMED_MESSAGE,
                                                                 "Could not parse incoming message!"));
        } catch (ServletException e) {
            if (log.isWarnEnabled()) {
                log.warn("incoming request was not multipart!");
                log.warn(e.getMessage(), e);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(createDefaultErrorMessage(RejectionReason.INTERNAL_RECIPIENT_ERROR,
                                                                 String.format(
                                                                         "Could not read incoming request! Error: %s",
                                                                         e.getMessage())));
        }
    }

    /**
     * Create a Spring {@link MultiValueMap} from a {@link java.util.Map}.
     *
     * @param map a map as provided by the MessageResponse
     * @return a MultiValueMap used as ResponseEntity for Spring
     */
    private MultiValueMap<String, Object> createMultiValueMap(final Map<String, Object> map) {
        if (log.isDebugEnabled()) {
            log.debug("Creating MultiValueMap for the response");
        }

        final var multiMap = new LinkedMultiValueMap<String, Object>();

        for (final var entry : map.entrySet()) {
            multiMap.put(entry.getKey(), List.of(entry.getValue()));
        }

        return multiMap;
    }

    /**
     * Create a default RejectionMessage with a given RejectionReason and specific error message for the payload.
     *
     * @param rejectionReason reason why the message was rejected
     * @param errorMessage    a specific error message for the payload
     * @return MultiValueMap with given error information that can be used for a multipart response
     */
    private MultiValueMap<String, Object> createDefaultErrorMessage(final RejectionReason rejectionReason,
                                                                    final String errorMessage) {
        try {
            final var rejectionMessage = new RejectionMessageBuilder()
                    ._securityToken_(
                            new DynamicAttributeTokenBuilder()
                                    ._tokenFormat_(TokenFormat.JWT)
                                    ._tokenValue_("rejected!")
                                    .build())
                    ._correlationMessage_(URI.create("https://INVALID"))
                    ._senderAgent_(configContainer.getConnector().getId())
                    ._modelVersion_(configContainer.getConnector().getOutboundModelVersion())
                    ._rejectionReason_(rejectionReason)
                    ._issuerConnector_(configContainer.getConnector().getId())
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    .build();

            final var multiMap = new LinkedMultiValueMap<String, Object>();
            multiMap.put(MultipartDatapart.HEADER.toString(), List.of(serializer.serialize(rejectionMessage)));
            multiMap.put(MultipartDatapart.PAYLOAD.toString(), List.of(errorMessage));

            return multiMap;
        } catch (IOException e) {
            if (log.isInfoEnabled()) {
                log.info(e.getMessage(), e);
            }
            return null;
        }
    }
}
