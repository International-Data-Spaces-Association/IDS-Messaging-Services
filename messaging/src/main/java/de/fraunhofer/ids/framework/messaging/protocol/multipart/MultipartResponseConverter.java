package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.io.File;
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
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ArtifactRequestMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ArtifactResponseMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ContractAgreementMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ContractOfferMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ContractRejectionMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ContractRequestMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ContractResponseMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.DescriptionRequestMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.InfrastructurePayloadMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ParticipantNotificationMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ParticipantRequestMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.QueryMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.RejectionMAP;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.ResourceMAP;
import de.fraunhofer.ids.framework.util.MultipartDatapart;
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
        //var responseMap = MultipartParser.stringToMultipart(Objects.requireNonNull(response.body()).string());
        //var messageString = responseMap.get(MultipartDatapart.HEADER.toString());
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
            //ToDo: Write Artifact to temp file
            //File artifactTmp = Files.createTempFile("tmp", payloadString.getFilename()).toFile();
            //FileUtils.writeByteArrayToFile(artifactTmp, payload.getSerialization());
            final var artifactTmp = new File("tmp");

            return new ArtifactResponseMAP((ArtifactResponseMessage) message, artifactTmp);
        }

        // broker-related messages
        else if (message instanceof ConnectorUpdateMessage) {
            return new InfrastructurePayloadMAP(message, serializer.deserialize(payloadString, InfrastructureComponent.class));
        } else if (message instanceof ConnectorUnavailableMessage) {
            return new InfrastructurePayloadMAP(message, null);
        } else if (message instanceof QueryMessage) {
            return new QueryMAP((QueryMessage) message, payloadString);
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
