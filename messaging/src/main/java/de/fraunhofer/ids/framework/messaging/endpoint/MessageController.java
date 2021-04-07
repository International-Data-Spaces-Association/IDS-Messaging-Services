package de.fraunhofer.ids.framework.messaging.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * REST controller for handling all incoming IDS multipart Messages
 */
@Slf4j
@Controller
public class MessageController {
    private final MessageDispatcher messageDispatcher;
    private final ConfigContainer   configContainer;
    private final Serializer        serializer;

    @Autowired
    public MessageController( final MessageDispatcher messageDispatcher,
                              final Serializer serializer,
                              final ConfigContainer configContainer ) {
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
     *
     * @return multipart MultivalueMap containing ResponseMessage header and some payload
     */
    @Transactional
    public ResponseEntity<MultiValueMap<String, Object>> handleIDSMessage( final HttpServletRequest request ) {
        try {
            final var headerPart = request.getPart(MultipartDatapart.HEADER.toString());
            final var payloadPart = request.getPart(MultipartDatapart.PAYLOAD.toString());

            if( headerPart == null ) {
                log.debug("header of incoming message were empty!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(createDefaultErrorMessage(RejectionReason.MALFORMED_MESSAGE,
                                                                     "Header was missing!"));
            }

            String input;
            log.debug("parsing header of incoming message");
            try( Scanner scanner = new Scanner(headerPart.getInputStream(), StandardCharsets.UTF_8.name()) ) {
                input = scanner.useDelimiter("\\A").next();
            }

            if(!checkInboundVersion(input)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createDefaultErrorMessage(RejectionReason.VERSION_NOT_SUPPORTED, "Infomodel Version of incoming Message not supported"));
            }

            // Deserialize JSON-LD headerPart to its RequestMessage.class
            final var requestHeader = serializer.deserialize(input, Message.class);

            log.debug("hand the incoming message to the message dispatcher!");
            final var response = this.messageDispatcher.process(requestHeader,
                                                                payloadPart == null ? null : payloadPart.getInputStream()); //pass null if payloadPart is null, else pass it as inputStream

            if( response != null ) {
                //get Response as MultiValueMap
                final var responseAsMap = createMultiValueMap(response.createMultipartMap(serializer));

                // return the ResponseEntity as Multipart content with created MultiValueMap
                log.debug("sending response with status OK (200)");
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(responseAsMap);
            } else {
                //if no response-body specified by the implemented handler of the connector (e.g. for received RequestInProcessMessage)
                log.warn("Implemented Message-Handler didn't return a response!");
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .build();
            }
        } catch( PreDispatchingFilterException e ) {
            log.error("Error during pre-processing with a PreDispatchingFilter!", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(createDefaultErrorMessage(RejectionReason.BAD_PARAMETERS,
                                                                 String.format("Error during preprocessing: %s",
                                                                               e.getMessage())));
        } catch( IOException e ) {
            log.warn("incoming message could not be parsed!");
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(createDefaultErrorMessage(RejectionReason.MALFORMED_MESSAGE,
                                                                 "Could not parse incoming message!"));
        } catch( ServletException e ) {
            log.warn("incoming request was not multipart!");
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(createDefaultErrorMessage(RejectionReason.INTERNAL_RECIPIENT_ERROR,
                                                                 String.format(
                                                                         "Could not read incoming request! Error: %s",
                                                                         e.getMessage())));
        }
    }

    /**
     * Create a Spring {@link MultiValueMap} from a {@link java.util.Map}
     *
     * @param map a map as provided by the MessageResponse
     *
     * @return a MultiValueMap used as ResponseEntity for Spring
     */
    private MultiValueMap<String, Object> createMultiValueMap( final Map<String, Object> map ) {
        log.debug("Creating MultiValueMap for the response");
        var multiMap = new LinkedMultiValueMap<String, Object>();

        for( var entry : map.entrySet() ) {
            multiMap.put(entry.getKey(), List.of(entry.getValue()));
        }

        return multiMap;
    }

    /**
     * Create a default RejectionMessage with a given RejectionReason and specific error message for the payload
     *
     * @param rejectionReason reason why the message was rejected
     * @param errorMessage    a specific error message for the payload
     *
     * @return MultiValueMap with given error information that can be used for a multipart response
     */
    private MultiValueMap<String, Object> createDefaultErrorMessage( final RejectionReason rejectionReason,
                                                                     final String errorMessage ) {
        try {
            var rejectionMessage = new RejectionMessageBuilder()
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

            var multiMap = new LinkedMultiValueMap<String, Object>();
            multiMap.put(MultipartDatapart.HEADER.toString(), List.of(serializer.serialize(rejectionMessage)));
            multiMap.put(MultipartDatapart.PAYLOAD.toString(), List.of(errorMessage));

            return multiMap;
        } catch( IOException e ) {
            log.info(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @param message incoming infomodel message
     * @return true if infomodel version is supported
     */
    private boolean checkInboundVersion(Message message){
        var inboundList = configContainer.getConfigurationModel()
                .getConnectorDescription()
                .getInboundModelVersion();
        return inboundList.stream()
                .map(version -> checkInfomodelContainment(message.getModelVersion(), version))
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    /**
     * @param input controllers header input as string
     * @return true if infomodel version is supported
     * @throws IOException if no infomodel version is found in input
     */
    private boolean checkInboundVersion(String input) throws IOException {
        JsonNode jsonInput = new ObjectMapper().readTree(input);
        if (jsonInput.has("ids:modelVersion")){
            var inboundList = configContainer.getConfigurationModel()
                    .getConnectorDescription()
                    .getInboundModelVersion();
                    return inboundList.stream()
                            .map(version -> checkInfomodelContainment(input, version))
                            .reduce(Boolean::logicalOr)
                            .orElse(false);
        }else {
            throw new IOException("No ModelVersion in incoming header!");
        }
    }

    /**
     * @param input input infomodel version (eg 4.0.1)
     * @param accepted accepted infomodel version (eg 4.0.2, supports wildcards eg 4.*.*)
     * @return true if infomodel input is covered by accepted input
     */
    private boolean checkInfomodelContainment(String input, String accepted){
        if(input.equals(accepted)) return true;
        var acceptedSplit = accepted.split("\\.");
        var inputSplit = input.split("\\.");
        if(inputSplit.length != acceptedSplit.length) return false;
        for(int i=0; i< inputSplit.length; i++){
           if(inputSplit[i].equals(acceptedSplit[i]) || acceptedSplit[i].equals("*")) return true;
        }
        return false;
    }
}
