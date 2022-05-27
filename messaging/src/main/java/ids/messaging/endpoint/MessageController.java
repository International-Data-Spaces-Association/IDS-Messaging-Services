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
package ids.messaging.endpoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.dispatcher.MessageDispatcher;
import ids.messaging.dispatcher.filter.PreDispatchingFilterException;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import ids.messaging.util.IdsMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * The MessageDispatcher.
     */
    private final MessageDispatcher messageDispatcher;

    /**
     * The ConfigContainer.
     */
    private final ConfigContainer configContainer;

    /**
     * The infomodel serializer.
     */
    private final Serializer serializer;

    /**
     * Used to switch incoming infomodel version compatibility check off or on (default on).
     */
    @Value("#{new Boolean('${infomodel.compatibility.validation:true}')}")
    private Boolean validateInfVer;

    /**
     * Used to switch logging incoming requests off or on (default off).
     */
    @Value("#{new Boolean('${messaging.log.incoming:false}')}")
    private Boolean logIncoming;

    /**
     * Used to switch logging send responses to incoming requests off or on (default off).
     */
    @Value("#{new Boolean('${messaging.log.outgoing:false}')}")
    private Boolean logResponse;

    /**
     * Constructor for the MessageController.
     * @param messageDispatcher The MessageDispatcher.
     * @param serializer The infomodel serializer.
     * @param configContainer The ConfigContainer.
     */
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
     * Get header and payload from incoming message, let the MessageDispatcher and
     * MessageHandler process it and return the result as a Multipart response.
     *
     * @param request Incoming http request.
     * @return Multipart MultivalueMap containing ResponseMessage header and some payload.
     */
    public ResponseEntity<MultiValueMap<String, Object>> handleIDSMessage(
            final HttpServletRequest request) {
        try {
            if (log.isInfoEnabled()) {
                log.info("Received incoming message. [code=(IMSMEI0059)]");
            }

            final var headerPart =
                    request.getPart(MultipartDatapart.HEADER.toString());
            final var payloadPart =
                    request.getPart(MultipartDatapart.PAYLOAD.toString());

            if (headerPart == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Header of incoming message were empty! [code=(IMSMED0119)]");
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(createDefaultErrorMessage(
                                             RejectionReason.MALFORMED_MESSAGE,
                                             "Header was missing!"));
            }

            final var headerBytes = IOUtils.toByteArray(headerPart.getInputStream());
            logIncomingMessage(headerBytes);
            String input;

            if (log.isDebugEnabled()) {
                log.debug("Parsing header of incoming message. [code=(IMSMED0120)]");
            }

            final var headerInput = new ByteArrayInputStream(headerBytes);

            try (var scanner = new Scanner(headerInput,
                                           StandardCharsets.UTF_8.name())) {
                input = scanner.useDelimiter("\\A").next();
            }

            headerInput.close();

            final var infomodelCompability = validateInfomodelVersion(input);

            if (infomodelCompability.isPresent()) {
                final var errorMessage = infomodelCompability.get();

                if (log.isWarnEnabled()) {
                    log.warn("Infomodel model version validation of received messages is switched"
                             + " on. Model-version of incoming message not supported."
                             + " Sending BAD_REQUEST response as a result."
                             + " [code=(IMSMEW0042), response-message=({})]",
                             errorMessage);
                }

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(createDefaultErrorMessage(
                                RejectionReason.VERSION_NOT_SUPPORTED,
                                errorMessage));
            }

            // Deserialize JSON-LD headerPart to its RequestMessage.class
            final var requestHeader = serializer.deserialize(input, Message.class);

            if (log.isDebugEnabled()) {
                log.debug("Hand the incoming message to the message dispatcher!"
                          + " [code=(IMSMED0121)]");
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
                if (log.isInfoEnabled()) {
                    log.info("Sending response with status OK (200). [code=(IMSMEI0061)]");
                }

                logResponseHeader(responseAsMap);

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(responseAsMap);
            } else {
                //if no response-body specified by the implemented handler
                // of the connector (e.g. for received RequestInProcessMessage)

                if (log.isDebugEnabled()) {
                    log.debug("Implemented Message-Handler didn't return a response,"
                              + " sending status OK instead as response! [code=(IMSMED0122)]");
                }

                if (log.isInfoEnabled()) {
                    log.info("Sending response with status OK (200) without body."
                            + " [code=(IMSMEI0062)]");
                }

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .build();
            }
        } catch (PreDispatchingFilterException e) {
            if (log.isErrorEnabled()) {
                log.error("Error during pre-processing with a PreDispatchingFilter!"
                          + " Sending BAD_REQUEST as response."
                          + " [code=(IMSMEE0021), exception=({})]", e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(createDefaultErrorMessage(
                                     RejectionReason.BAD_PARAMETERS,
                                     String.format(
                                         "Error during preprocessing: %s", e.getMessage())));
        } catch (IOException | SerializeException e) {
            if (log.isWarnEnabled()) {
                log.warn("Incoming message could not be parsed, sending response BAD_REQUEST"
                         + " with RejectionReason.MALFORMED_MESSAGE! [code=(IMSMEW0043),"
                         + " exception=({})]", e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(createDefaultErrorMessage(
                                         RejectionReason.MALFORMED_MESSAGE,
                                         "Could not parse incoming message!"));
        } catch (ServletException e) {
            if (log.isWarnEnabled()) {
                log.warn("Incoming request was not multipart!"
                         + " Sending INTERNAL_SERVER_ERROR as response [code=(IMSMEW0044),"
                         + " exception=({})]", e.getMessage());
            }

            return ResponseEntity
                         .status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .body(createDefaultErrorMessage(
                             RejectionReason.INTERNAL_RECIPIENT_ERROR,
                             String.format(
                                 "Could not read incoming request! Error: %s", e.getMessage())));
        }
    }

    private void logIncomingMessage(final byte[] headerBytes) throws IOException {
        if (Boolean.TRUE.equals(logIncoming)) {
            final var headerInput = new ByteArrayInputStream(headerBytes);
            log.info("Incoming message header: {} [code=(IMSMEI0060)]",
                    IOUtils.toString(headerInput, StandardCharsets.UTF_8));
            headerInput.close();
        }
    }

    private void logResponseHeader(final MultiValueMap<String, Object> responseAsMap) {
        if (Boolean.TRUE.equals(logResponse)) {
            final var header = responseAsMap.get(MultipartDatapart.HEADER.toString()).toString();
            log.info("Send response header: {} [code=(IMSMEI0063)]", header);
        }
    }

    /**
     * Decides whether to run the infomodel compatibility check and if so, whether the
     * incoming message is compatible.
     *
     * @param input The received message.
     * @return Optional<String> Empty if successful or skipped, else error message as string.
     * @throws IOException No model version information found in the header.
     */
    private Optional<String> validateInfomodelVersion(final String input) throws IOException {
        if (validateInfVer) {
            return checkInboundVersion(input);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Skipped validating infomodel compability! [code=(IMSMED0123)]");
            }
            return Optional.empty();
        }
    }

    /**
     * Create a Spring {@link MultiValueMap} from a {@link java.util.Map}.
     *
     * @param map A map as provided by the MessageResponse.
     * @return A MultiValueMap used as ResponseEntity for Spring.
     */
    private MultiValueMap<String, Object> createMultiValueMap(
            final Map<String, Object> map) {
        if (log.isDebugEnabled()) {
            log.debug("Creating MultiValueMap for the response... [code=(IMSMED0124)]");
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
     * @param rejectionReason Reason why the message was rejected.
     * @param errorMessage A specific error message for the payload.
     * @return MultiValueMap with given error information that can
     * be used for a multipart response.
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
            if (log.isErrorEnabled()) {
                log.error("Serializer threw exception while creating default rejection message!"
                          + " [code=(IMSMEE0022), exception=({})]", e.getMessage());
            }
            return null;
        }
    }

    /**
     * @param input Controllers header input as string.
     * @return Optional<String> Empty if successful, else error message as string payload.
     * @throws IOException If no infomodel version is found in input.
     */
    private Optional<String> checkInboundVersion(final String input) throws IOException {
        final var jsonInput = new ObjectMapper().readTree(input);

        if (jsonInput.has("ids:modelVersion")) {
            final var inputVersion = jsonInput.get("ids:modelVersion")
                                              .textValue();
            final var inboundList = configContainer.getConfigurationModel()
                    .getConnectorDescription()
                    .getInboundModelVersion();

            final var compatible = inboundList.stream()
                                              .map(supportedVersion -> checkInfomodelContainment(
                                                      inputVersion, supportedVersion))
                                              .reduce(Boolean::logicalOr)
                                              .orElse(false);

            if (!compatible) {
                final var message = "Infomodel version of incoming Message not in"
                                    + " supported inbound model version list!"
                                    + " [incoming=(" + inputVersion + "),"
                                    + " supported=(" + inboundList + ")]";
                return Optional.of(message);
            }

            return Optional.empty();
        } else {
            throw new IOException("No ModelVersion in incoming header!");
        }
    }

    /**
     * @param input Input infomodel version (eg 4.0.1).
     * @param accepted Accepted infomodel version (eg 4.0.2, supports wildcards eg 4.*.*).
     * @return True if infomodel input is covered by accepted input.
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
