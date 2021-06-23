package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.*;
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
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
@Service
public class RequestTemplateProvider {

    ConfigContainer container;
    DapsTokenProvider tokenProvider;

    public RequestMessageTemplate<DescriptionRequestMessage> descriptionRequestMessageTemplate(URI requestedElement){
        return () -> new DescriptionRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._requestedElement_(requestedElement)
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public RequestMessageTemplate<ArtifactRequestMessage> artifactRequestMessageTemplate(URI requestedArtifact){
        return () -> new ArtifactRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._requestedArtifact_(requestedArtifact)
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public RequestMessageTemplate<ContractRequestMessage> contractRequestMessageTemplate(){
        return () -> new ContractRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public RequestMessageTemplate<UploadMessage> uploadMessageTemplate(){
        return () -> new UploadMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public RequestMessageTemplate<ParticipantRequestMessage> participantRequestMessageTemplate(URI requestedParticipant){
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
