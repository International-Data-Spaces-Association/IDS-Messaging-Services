package de.fraunhofer.ids.messaging.protocol.multipart;
import java.io.IOException;
import java.util.Map;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.core.util.MultipartDatapart;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.*;
import okhttp3.Response;

/**
 * Converts a {@link Response} into the corresponding {@link MessageAndPayload object}.
 */
public class MultipartResponseConverter {
    private final Serializer serializer = new Serializer();

    /**
     *
     * @param responseMap in a Map
     * @return MessageAndPayload containing the corresponding Message and the payload parsed in Infomodel classes
     * @throws IOException if message or payload cannot be parsed
     */
    public MessageAndPayload<?, ?> convertResponse(final Map<String, String> responseMap) throws IOException {
        final var payloadString = responseMap.getOrDefault(MultipartDatapart.PAYLOAD.toString(), "");
        final var message = serializer.deserialize(responseMap.getOrDefault(MultipartDatapart.HEADER.toString(), ""), Message.class);

        // core message types
        if (message instanceof DescriptionRequestMessage) {
            return new DescriptionRequestMAP((DescriptionRequestMessage) message);
        } else if (message instanceof DescriptionResponseMessage) {
            return new DescriptionResponseMAP(
                    (DescriptionResponseMessage) message, payloadString);
        } else if (message instanceof MessageProcessedNotificationMessage) {
            return new MessageProcessedNotificationMAP((MessageProcessedNotificationMessage) message);
        } else if (message instanceof RejectionMessage) {
            if (message instanceof ContractRejectionMessage) {
                return new ContractRejectionMAP((ContractRejectionMessage) message);
            }
            return new RejectionMAP((RejectionMessage) message, payloadString);
        }

        // artifact-related message types
        else if (message instanceof ArtifactRequestMessage) {
            return new ArtifactRequestMAP((ArtifactRequestMessage) message);
        } else if (message instanceof ArtifactResponseMessage) {
            return new ArtifactResponseMAP((ArtifactResponseMessage) message, payloadString);
        }

        // broker-related messages
        else if (message instanceof ConnectorUpdateMessage) {
            return new InfrastructurePayloadMAP(message, serializer.deserialize(payloadString, InfrastructureComponent.class));
        } else if (message instanceof ConnectorUnavailableMessage) {
            return new InfrastructurePayloadMAP(message, null);
        } else if (message instanceof QueryMessage) {
            return new QueryMAP((QueryMessage) message, payloadString);
        } else if (message instanceof ResultMessage ) {
            return new ResultMAP((ResultMessage) message, payloadString);
        } else if (message instanceof ResourceUpdateMessage) {
            return new ResourceMAP(message, serializer.deserialize(payloadString, Resource.class));
        } else if (message instanceof ResourceUnavailableMessage) {
            return new ResourceMAP(message);
        }

        // participant-related messages
        else if (message instanceof ParticipantUpdateMessage) {
            return new ParticipantNotificationMAP(message, serializer.deserialize(payloadString, Participant.class));
        } else if (message instanceof ParticipantUnavailableMessage) {
            return new ParticipantNotificationMAP(message);
        } else if (message instanceof ParticipantRequestMessage) {
            return new ParticipantRequestMAP((ParticipantRequestMessage) message);
        }

        // contract-related messages
        else if (message instanceof ContractOfferMessage) {
            return new ContractOfferMAP((ContractOfferMessage) message, serializer.deserialize(payloadString, ContractOffer.class));
        } else if (message instanceof ContractRequestMessage) {
            return new ContractRequestMAP((ContractRequestMessage) message, serializer.deserialize(payloadString, ContractRequest.class));
        } else if (message instanceof ContractAgreementMessage) {
            return new ContractAgreementMAP((ContractAgreementMessage) message, serializer.deserialize(payloadString, ContractAgreement.class));
        } else if (message instanceof ContractResponseMessage) {
            return new ContractResponseMAP((ContractResponseMessage) message, serializer.deserialize(payloadString, ContractOffer.class));
        }
        throw new IOException("Could not convert input header to suitable message and payload type. Header: " + message.toRdf());
    }
}
