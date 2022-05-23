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
package ids.messaging.response;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import ids.messaging.util.IdsMessageUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of MessageResponse used for returning RejectionMessages and Error descriptions.
 */
@Data
@Slf4j
@AllArgsConstructor
public class ErrorResponse implements MessageResponse {
    /**
     * The message (RejectionMessage).
     */
    private final RejectionMessage rejectionMessage;

    /**
     * The error message.
     */
    private final String  errorMessage;

    /**
     * Create an ErrorResponse with a RejectionMessage header and errorReason String payload.
     *
     * @param rejectionMessage A RejectionMessage.
     * @param errorReason A detailed Error description.
     *
     * @return An instance of ErrorResponse with the given parameters.
     */
    public static ErrorResponse create(final RejectionMessage rejectionMessage,
                                       final String errorReason) {
        return new ErrorResponse(rejectionMessage, errorReason);
    }

    /**
     * Create an ErrorResponse with Default RejectionMessage as header
     * (only RejectionReason has to be Provided).
     *
     * @param rejectionReason RejectionReason (why the message was rejected).
     * @param errorMessage Detailed error description.
     * @param connectorId ID of the current connector.
     * @param modelVersion Infomodelversion of the current connector.
     * @param messageId ID of the message being rejected (from message-header).
     * @return An instance of ErrorResponse with the given parameters.
     */
    public static ErrorResponse withDefaultHeader(
            final RejectionReason rejectionReason,
            final String errorMessage,
            final URI connectorId,
            final String modelVersion,
            final URI messageId) {

        var builderMessageId = messageId;
        if (messageId == null) {
            builderMessageId = URI.create("https://INVALID");
        }

        final var rejectionMessage = new RejectionMessageBuilder()
                ._securityToken_(
                        new DynamicAttributeTokenBuilder()
                                ._tokenFormat_(TokenFormat.JWT)
                                ._tokenValue_("rejected!")
                                .build())
                ._correlationMessage_(builderMessageId)
                ._senderAgent_(connectorId)
                ._issuerConnector_(connectorId)
                ._modelVersion_(modelVersion)
                ._rejectionReason_(rejectionReason)
                ._issued_(IdsMessageUtils.getGregorianNow())
                .build();

        return ErrorResponse.create(rejectionMessage, errorMessage);
    }

    /**
     * Create an ErrorResponse with Default RejectionMessage as header (only RejectionReason
     * has to be Provided).
     *
     * @param rejectionReason RejectionReason (why the message was rejected).
     * @param errorMessage Detailed error description.
     * @param connectorId ID of the current connector.
     * @param modelVersion Infomodelversion of the current connector.
     * @return An instance of ErrorResponse with the given parameters.
     */
    public static ErrorResponse withDefaultHeader(
            final RejectionReason rejectionReason, final String errorMessage,
            final URI connectorId, final String modelVersion) {
        return withDefaultHeader(rejectionReason, errorMessage, connectorId, modelVersion, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createMultipartMap(final Serializer serializer)
            throws SerializeException {
        try {
            final var multiMap = new LinkedHashMap<String, Object>();
            multiMap.put(MultipartDatapart.HEADER.toString(),
                         serializer.serialize(rejectionMessage));
            multiMap.put(MultipartDatapart.PAYLOAD.toString(), errorMessage);

            return multiMap;
        } catch (IOException ioException) {
            throw new SerializeException(ioException);
        }
    }
}
