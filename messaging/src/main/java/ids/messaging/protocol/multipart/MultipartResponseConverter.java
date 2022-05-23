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
package ids.messaging.protocol.multipart;

import java.io.IOException;
import java.util.Map;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferMessage;
import de.fraunhofer.iais.eis.ContractRejectionMessage;
import de.fraunhofer.iais.eis.ContractRequest;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.ContractResponseMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.ParticipantRequestMessage;
import de.fraunhofer.iais.eis.ParticipantUnavailableMessage;
import de.fraunhofer.iais.eis.ParticipantUpdateMessage;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceUnavailableMessage;
import de.fraunhofer.iais.eis.ResourceUpdateMessage;
import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.DeserializeException;
import ids.messaging.protocol.multipart.mapping.ArtifactRequestMAP;
import ids.messaging.protocol.multipart.mapping.ArtifactResponseMAP;
import ids.messaging.protocol.multipart.mapping.ContractAgreementMAP;
import ids.messaging.protocol.multipart.mapping.ContractOfferMAP;
import ids.messaging.protocol.multipart.mapping.ContractRejectionMAP;
import ids.messaging.protocol.multipart.mapping.ContractRequestMAP;
import ids.messaging.protocol.multipart.mapping.ContractResponseMAP;
import ids.messaging.protocol.multipart.mapping.DescriptionRequestMAP;
import ids.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import ids.messaging.protocol.multipart.mapping.InfrastructurePayloadMAP;
import ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import ids.messaging.protocol.multipart.mapping.ParticipantNotificationMAP;
import ids.messaging.protocol.multipart.mapping.ParticipantRequestMAP;
import ids.messaging.protocol.multipart.mapping.QueryMAP;
import ids.messaging.protocol.multipart.mapping.RejectionMAP;
import ids.messaging.protocol.multipart.mapping.ResourceMAP;
import ids.messaging.protocol.multipart.mapping.ResultMAP;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Converts a Response into the corresponding MessageAndPayload object.
 */
@Slf4j
@NoArgsConstructor
public class MultipartResponseConverter {

    /**
     * The infomodel serializer.
     */
    private final Serializer serializer = new Serializer();

    /**
     * Converts a Response into a corresponding MessageAndPayload Object.
     *
     * @param responseMap Response in a Map.
     * @return MessageAndPayload containing the corresponding Message and the payload parsed
     * in Infomodel classes.
     * @throws UnknownResponseException If message or payload cannot be parsed.
     * @throws DeserializeException If problems occur deseralizing the message.
     */
    public MessageAndPayload<?, ?> convertResponse(
            final Map<String, String> responseMap)
            throws UnknownResponseException, DeserializeException {
        //The return param
        MessageAndPayload<?, ?> messageAndPayload = null;

        Message responseHeader; //The response "message", set in try catch

        final var responsePayload = getResponsePayload(responseMap);

        try {
            //Can throw IOException if Header can not be deserialized
            responseHeader = getResponseHeader(responseMap);
        } catch (IOException ioException) {
            if (log.isErrorEnabled()) {
                log.error("Error deserializing Header! [code=(IMSMEE0023),"
                          + " exception=({})]", ioException.getMessage());
            }
            throw new DeserializeException(ioException);
        }

        try {
            if (responseHeader instanceof DescriptionRequestMessage) {
                messageAndPayload = getDescriptionRequestMessage(responseHeader);
            } else if (responseHeader instanceof DescriptionResponseMessage) {
                messageAndPayload = getDescriptionResponseMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof MessageProcessedNotificationMessage) {
                messageAndPayload = getMessageProcessedNotificationMessage(responseHeader);
            } else if (responseHeader instanceof RejectionMessage) {
                messageAndPayload = getRejectionMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ArtifactRequestMessage) {
                messageAndPayload = getArtifactRequestMessage(responseHeader);
            } else if (responseHeader instanceof ArtifactResponseMessage) {
                messageAndPayload = getArtifactResponseMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ConnectorUpdateMessage) {
                messageAndPayload = getConnectorUpdateMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ConnectorUnavailableMessage) {
                messageAndPayload = getConnectorUnavailableMessage(responseHeader);
            } else if (responseHeader instanceof QueryMessage) {
                messageAndPayload = getQueryMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ResultMessage) {
                messageAndPayload = getResultMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ResourceUpdateMessage) {
                messageAndPayload = getResourceUpdateMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ResourceUnavailableMessage) {
                messageAndPayload = getResourceUnavailableMessage(responseHeader);
            } else if (responseHeader instanceof ParticipantUpdateMessage) {
                messageAndPayload = getParticipantUpdateMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ParticipantUnavailableMessage) {
                messageAndPayload = getParticipantUnavailableMessage(responseHeader);
            } else if (responseHeader instanceof ParticipantRequestMessage) {
                messageAndPayload = getParticipantRequestMessage(responseHeader);
            } else if (responseHeader instanceof ContractOfferMessage) {
                messageAndPayload = getContractOfferMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ContractRequestMessage) {
                messageAndPayload = getContractRequestMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ContractAgreementMessage) {
                messageAndPayload = getContractAgreementMessage(responseHeader, responsePayload);
            } else if (responseHeader instanceof ContractResponseMessage) {
                messageAndPayload = getContractResponseMessage(responseHeader, responsePayload);
            }

            if (messageAndPayload != null) {
                //Match found, returning messageAndPayload
                return messageAndPayload;
            } else {
                //No match found, throw UnknownResponseException
                if (log.isErrorEnabled()) {
                    log.error("Could not convert input header to suitable responseHeader"
                              + " and payload type! [code=(IMSMEE0024)]");
                }
                throw new UnknownResponseException(
                        "Could not convert input header to suitable responseHeader and payload"
                        + " type. Header: " + responseHeader.toRdf());
            }
        } catch (IOException ioException) {
            //Deserializing Payload threw exception
            throw new DeserializeException(ioException);
        }
    }

    @NotNull
    private MessageAndPayload<?, ?> getContractResponseMessage(
            final Message responseHeader,
            final String responsePayload)
            throws IOException {
        return new ContractResponseMAP(
               (ContractResponseMessage) responseHeader,
               serializer.deserialize(responsePayload, ContractOffer.class));
    }

    @NotNull
    private MessageAndPayload<?, ?> getContractAgreementMessage(
            final Message responseHeader,
            final String responsePayload)
            throws IOException {
        return new ContractAgreementMAP(
            (ContractAgreementMessage) responseHeader,
            serializer.deserialize(responsePayload, ContractAgreement.class));
    }

    @NotNull
    private MessageAndPayload<?, ?> getContractRequestMessage(
            final Message responseHeader,
            final String responsePayload)
            throws IOException {
        return new ContractRequestMAP((ContractRequestMessage) responseHeader,
              serializer.deserialize(responsePayload, ContractRequest.class));
    }

    @NotNull
    private MessageAndPayload<?, ?> getContractOfferMessage(
            final Message responseHeader,
            final String responsePayload)
            throws IOException {
        return new ContractOfferMAP((ContractOfferMessage) responseHeader,
                serializer.deserialize(responsePayload, ContractOffer.class));
    }

    @NotNull
    private MessageAndPayload<?, ?> getParticipantRequestMessage(
            final Message responseHeader) {
        return new ParticipantRequestMAP(
                (ParticipantRequestMessage) responseHeader);
    }

    @NotNull
    private MessageAndPayload<?, ?> getParticipantUnavailableMessage(
            final Message responseHeader) {
        return new ParticipantNotificationMAP(responseHeader);
    }

    @NotNull
    private MessageAndPayload<?, ?> getParticipantUpdateMessage(
            final Message responseHeader,
            final String responsePayload)
            throws IOException {
        return new ParticipantNotificationMAP(responseHeader, serializer
                .deserialize(responsePayload, Participant.class));
    }

    @NotNull
    private MessageAndPayload<?, ?> getResourceUnavailableMessage(
            final Message responseHeader) {
        return new ResourceMAP(responseHeader);
    }

    @NotNull
    private MessageAndPayload<?, ?> getResourceUpdateMessage(
            final Message responseHeader,
            final String responsePayload)
            throws IOException {
        return new ResourceMAP(responseHeader, serializer
                .deserialize(responsePayload, Resource.class));
    }

    @NotNull
    private MessageAndPayload<?, ?> getResultMessage(
            final Message responseHeader,
            final String responsePayload) {
        return new ResultMAP((ResultMessage) responseHeader, responsePayload);
    }

    @NotNull
    private MessageAndPayload<?, ?> getQueryMessage(
            final Message responseHeader,
            final String responsePayload) {
        return new QueryMAP((QueryMessage) responseHeader, responsePayload);
    }

    @NotNull
    private MessageAndPayload<?, ?> getConnectorUnavailableMessage(
            final Message responseHeader) {
        return new InfrastructurePayloadMAP(responseHeader, null);
    }

    @NotNull
    private MessageAndPayload<?, ?> getConnectorUpdateMessage(
            final Message responseHeader,
            final String responsePayload)
            throws IOException {
        return new InfrastructurePayloadMAP(
                responseHeader,
                serializer.deserialize(
                        responsePayload,
                        InfrastructureComponent.class));
    }

    @NotNull
    private MessageAndPayload<?, ?> getArtifactResponseMessage(
            final Message responseHeader,
            final String responsePayload) {
        return new ArtifactResponseMAP(
                (ArtifactResponseMessage) responseHeader,
                responsePayload);
    }

    @NotNull
    private MessageAndPayload<?, ?> getArtifactRequestMessage(
            final Message responseHeader) {
        return new ArtifactRequestMAP((ArtifactRequestMessage) responseHeader);
    }

    @NotNull
    private MessageAndPayload<?, ?> getMessageProcessedNotificationMessage(
            final Message responseHeader) {
        return new MessageProcessedNotificationMAP(
                (MessageProcessedNotificationMessage) responseHeader);
    }

    @NotNull
    private MessageAndPayload<?, ?> getDescriptionResponseMessage(
            final Message responseHeader,
            final String responsePayload) {
        return new DescriptionResponseMAP(
                (DescriptionResponseMessage) responseHeader, responsePayload);
    }

    @NotNull
    private MessageAndPayload<?, ?> getDescriptionRequestMessage(
            final Message responseHeader) {
        return new DescriptionRequestMAP(
                (DescriptionRequestMessage) responseHeader);
    }

    @NotNull
    private MessageAndPayload<?, ?> getRejectionMessage(
            final Message responseHeader,
            final String responsePayload) {
        if (responseHeader instanceof ContractRejectionMessage) {
            return new ContractRejectionMAP(
                    (ContractRejectionMessage) responseHeader);
        } else {
            return new RejectionMAP((RejectionMessage) responseHeader,
                                    responsePayload);
        }
    }

    private Message getResponseHeader(final Map<String, String> responseMap)
            throws IOException {
        return serializer.deserialize(responseMap.getOrDefault(
                MultipartDatapart.HEADER.toString(), ""), Message.class);
    }

    private String getResponsePayload(final Map<String, String> responseMap) {
        return responseMap.getOrDefault(MultipartDatapart.PAYLOAD.toString(), "");
    }
}
