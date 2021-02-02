package de.fraunhofer.ids.framework.messaging.response;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of MessageResponse used for returning RejectionMessages and Error descriptions.
 */
@Data
@Slf4j
public class ErrorResponse implements MessageResponse {
    private final RejectionMessage rejectionMessage;
    private final String           errorMessage;

    /**
     * Create an ErrorResponse with a RejectionMessage header and errorReason String payload
     *
     * @param rejectionMessage a RejectionMessage
     * @param errorMessage     a detailed Error description
     */
    public ErrorResponse( RejectionMessage rejectionMessage, String errorMessage ) {
        this.rejectionMessage = rejectionMessage;
        this.errorMessage = errorMessage;
    }

    /**
     * Create an ErrorResponse with a RejectionMessage header and errorReason String payload
     *
     * @param rejectionMessage a RejectionMessage
     * @param errorReason      a detailed Error description
     *
     * @return an instance of ErrorResponse with the given parameters
     */
    public static ErrorResponse create( final RejectionMessage rejectionMessage, final String errorReason ) {
        return new ErrorResponse(rejectionMessage, errorReason);
    }

    /**
     * Create an ErrorResponse with Default RejectionMessage as header (only RejectionReason has to be Provided)
     *
     * @param rejectionReason RejectionReason (why the message was rejected)
     * @param errorMessage    detailed error description
     * @param connectorId     id of the current connector
     * @param modelVersion    infomodelversion of the current connector
     *
     * @return an instance of ErrorResponse with the given parameters
     */
    public static ErrorResponse withDefaultHeader( final RejectionReason rejectionReason, final String errorMessage,
                                                   final URI connectorId, final String modelVersion ) {
        var rejectionMessage = new RejectionMessageBuilder()
                ._securityToken_(
                        new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("rejected!")
                                                          .build())
                ._correlationMessage_(URI.create("https://INVALID"))
                ._senderAgent_(connectorId)
                ._issuerConnector_(connectorId)
                ._modelVersion_(modelVersion)
                ._rejectionReason_(rejectionReason)
                ._issued_(IdsMessageUtils.getGregorianNow())
                .build();
        return new ErrorResponse(rejectionMessage, errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createMultipartMap( Serializer serializer ) throws IOException {
        var multiMap = new LinkedHashMap<String, Object>();
        multiMap.put("header", serializer.serialize(rejectionMessage));
        multiMap.put("payload", errorMessage);
        return multiMap;
    }
}
