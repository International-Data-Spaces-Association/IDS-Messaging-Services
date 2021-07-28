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
package de.fraunhofer.ids.messaging.endpoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.dispatcher.MessageDispatcher;
import de.fraunhofer.ids.messaging.dispatcher.filter.PreDispatchingFilterException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartDatapart;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
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
     * Generic method to handle all incoming ids messages.
     * One Method to Rule them All.
     * Get header and payload from incoming message,
     * let the MessageDispatcher and MessageHandler process it
     * and return the result as a Multipart response.
     *
     * @param request incoming http request
     * @return multipart MultivalueMap containing ResponseMessage header and some payload
     */
    public ResponseEntity<MultiValueMap<String, Object>> handleIDSMessage(
            final HttpServletRequest request) {
        try {
            final var headerPart =
                    request.getPart(MultipartDatapart.HEADER.toString());
            final var payloadPart =
                    request.getPart(MultipartDatapart.PAYLOAD.toString());

            if (headerPart == null) {
                if (log.isDebugEnabled()) {
                    log.debug("header of incoming message were empty!");
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(createDefaultErrorMessage(
                                             RejectionReason.MALFORMED_MESSAGE,
                                             "Header was missing!"));
            }

            String input;

            if (log.isDebugEnabled()) {
                log.debug("parsing header of incoming message");
            }

            try (var scanner = new Scanner(headerPart.getInputStream(),
                                           StandardCharsets.UTF_8.name())) {
                input = scanner.useDelimiter("\\A").next();
            }

            if (!checkInboundVersion(input)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(createDefaultErrorMessage(
                             RejectionReason.VERSION_NOT_SUPPORTED,
                             "Infomodel Version of incoming Message not supported"));
            }

            // Deserialize JSON-LD headerPart to its RequestMessage.class
            final var requestHeader = serializer
                    .deserialize(input, Message.class);

            if (log.isDebugEnabled()) {
                log.debug("hand the incoming message"
                          + " to the message dispatcher!");
            }

            //pass null if payloadPart is null, else pass it as inputStream
            final var response = this.messageDispatcher
                    .process(requestHeader, payloadPart == null
                            ? null
                            : payloadPart.getInputStream());

            if (response != null) {
                //get Response as MultiValueMap
                final var responseAsMap = createMultiValueMap(
                        response.createMultipartMap(serializer));

                // return the ResponseEntity as Multipart content
                // with created MultiValueMap
                if (log.isDebugEnabled()) {
                    log.debug("sending response with status OK (200)");
                }

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(responseAsMap);
            } else {
                //if no response-body specified by the implemented handler
                // of the connector (e.g. for received RequestInProcessMessage)

                if (log.isWarnEnabled()) {
                    log.warn("Implemented Message-Handler"
                             + " didn't return a response!");
                }

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .build();
            }
        } catch (PreDispatchingFilterException e) {
            if (log.isErrorEnabled()) {
                log.error(
                        "Error during pre-processing with"
                        + " a PreDispatchingFilter! "
                        + e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(createDefaultErrorMessage(
                                     RejectionReason.BAD_PARAMETERS,
                                     String.format(
                                         "Error during preprocessing: %s",
                                         e.getMessage())));
        } catch (IOException | SerializeException e) {
            if (log.isWarnEnabled()) {
                log.warn("incoming message could not be parsed!");
                log.warn(e.getMessage(), e);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(createDefaultErrorMessage(
                                         RejectionReason.MALFORMED_MESSAGE,
                                         "Could not parse incoming message!"));
        } catch (ServletException e) {
            if (log.isWarnEnabled()) {
                log.warn("incoming request was not multipart!");
                log.warn(e.getMessage(), e);
            }

            return ResponseEntity
                         .status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .body(createDefaultErrorMessage(
                             RejectionReason.INTERNAL_RECIPIENT_ERROR,
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
    private MultiValueMap<String, Object> createMultiValueMap(
            final Map<String, Object> map) {
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
     * Create a default RejectionMessage with a given RejectionReason
     * and specific error message for the payload.
     *
     * @param rejectionReason reason why the message was rejected
     * @param errorMessage    a specific error message for the payload
     * @return MultiValueMap with given error information that can
     * be used for a multipart response
     */
    private MultiValueMap<String, Object> createDefaultErrorMessage(
            final RejectionReason rejectionReason,
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
                    ._modelVersion_(configContainer.getConnector()
                                                   .getOutboundModelVersion())
                    ._rejectionReason_(rejectionReason)
                    ._issuerConnector_(configContainer.getConnector().getId())
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    .build();

            final var multiMap = new LinkedMultiValueMap<String, Object>();
            multiMap.put(MultipartDatapart.HEADER.toString(),
                         List.of(serializer.serialize(rejectionMessage)));
            multiMap.put(MultipartDatapart.PAYLOAD.toString(),
                         List.of(errorMessage));

            return multiMap;
        } catch (IOException e) {
            if (log.isInfoEnabled()) {
                log.info(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * @param input controllers header input as string
     * @return true if infomodel version is supported
     * @throws IOException if no infomodel version is found in input
     */
    private boolean checkInboundVersion(final String input) throws IOException {
        final var jsonInput = new ObjectMapper().readTree(input);

        if (jsonInput.has("ids:modelVersion")) {
            final var inputVersion = jsonInput.get("ids:modelVersion")
                                              .textValue();
            final var inboundList = configContainer.getConfigurationModel()
                    .getConnectorDescription()
                    .getInboundModelVersion();
                    return inboundList.stream()
                            .map(supportedVersion -> checkInfomodelContainment(
                                inputVersion, supportedVersion))
                            .reduce(Boolean::logicalOr)
                            .orElse(false);
        } else {
            throw new IOException("No ModelVersion in incoming header!");
        }
    }

    /**
     * @param input input infomodel version (eg 4.0.1)
     * @param accepted accepted infomodel version (eg 4.0.2,
     *                 supports wildcards eg 4.*.*)
     * @return true if infomodel input is covered by accepted input
     */
    private boolean checkInfomodelContainment(final String input,
                                              final String accepted) {
        if (input.equals(accepted))  {
            return true;
        }

        final var acceptedSplit = accepted.split("\\.");
        final var inputSplit = input.split("\\.");

        if (inputSplit.length != acceptedSplit.length) {
            return false;
        }

        for (var i = 0; i < inputSplit.length; i++) {
           if (!inputSplit[i].equals(acceptedSplit[i])
               && !"*".equals(acceptedSplit[i])) {
               return false;
           }
        }
        return true;
    }
}
