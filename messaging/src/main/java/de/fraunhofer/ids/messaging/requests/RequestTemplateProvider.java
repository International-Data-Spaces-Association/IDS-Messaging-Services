package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.ContractRequestMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantRequestMessage;
import de.fraunhofer.iais.eis.ParticipantRequestMessageBuilder;
import de.fraunhofer.iais.eis.UploadMessage;
import de.fraunhofer.iais.eis.UploadMessageBuilder;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class RequestTemplateProvider {

    ConfigContainer container;
    DapsTokenProvider tokenProvider;

    /**
     * Template for DescriptionRequestMessages.
     *
     * @param requestedElement requested element ID, or null if selfdescription is requested
     * @return template to build a {@link DescriptionRequestMessage}
     */
    public RequestMessageTemplate<DescriptionRequestMessage> descriptionRequestMessageTemplate(final URI requestedElement) {
        return () -> new DescriptionRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._requestedElement_(requestedElement)
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    /**
     * Template for ArtifactRequestMessages.
     *
     * @param requestedArtifact ID of requested artifact
     * @return template to build a {@link ArtifactRequestMessage}
     */
    public RequestMessageTemplate<ArtifactRequestMessage> artifactRequestMessageTemplate(final URI requestedArtifact) {
        return () -> new ArtifactRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._requestedArtifact_(requestedArtifact)
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    /**
     * Template for ContractRequestMessage.
     *
     * @return template to build a {@link ContractRequestMessage}
     */
    public RequestMessageTemplate<ContractRequestMessage> contractRequestMessageTemplate() {
        return () -> new ContractRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    /**
     * Template for UploadMessages.
     *
     * @return template to build an {@link UploadMessage}
     */
    public RequestMessageTemplate<UploadMessage> uploadMessageTemplate() {
        return () -> new UploadMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    /**
     * Template for ParticipantRequestMessage.
     *
     * @param requestedParticipant ID of requested participant
     * @return template to build a {@link ParticipantRequestMessage}
     */
    public RequestMessageTemplate<ParticipantRequestMessage> participantRequestMessageTemplate(final URI requestedParticipant) {
        return () -> new ParticipantRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._requestedParticipant_(requestedParticipant)
                .build();

    }

}
