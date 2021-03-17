package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.util.MultipartDatapart;
import de.fraunhofer.ids.framework.util.MultipartParser;
import okhttp3.Response;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;

/**
 * Converts a {@link Response} into the corresponding {@link MessageAndPayload object}
 */
public class MultipartResponseConverter {
    final private Serializer serializer = new Serializer();

    /**
     *
     * @param response to a multipart request
     * @return MessageAndPayload containing the corresponding Message and the payload parsed in Infomodel classes
     * @throws IOException if message cannot be parsed
     */
    public MessageAndPayload<?,?> convertResponse( Response response ) throws IOException {
        var responseMap = MultipartParser.stringToMultipart(Objects.requireNonNull(response.body()).string());
        var messageString = responseMap.get(MultipartDatapart.HEADER.toString());
        var payloadString = responseMap.getOrDefault(MultipartDatapart.PAYLOAD.toString(),"");
        var message = serializer.deserialize(messageString, Message.class);

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
            File artifactTmp = new File("tmp");

            return new ArtifactResponseMAP((ArtifactResponseMessage) message, artifactTmp);
        }

        // broker-related messages
        else if (message instanceof ConnectorUpdateMessage) {
            return new InfrastructureComponentMAP(message, serializer.deserialize(payloadString, InfrastructureComponent.class));
        } else if (message instanceof ConnectorUnavailableMessage) {
            return new InfrastructureComponentMAP(message);
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
