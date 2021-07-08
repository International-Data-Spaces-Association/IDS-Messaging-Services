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
    public MessageTemplate<DescriptionRequestMessage> descriptionRequestMessageTemplate(final URI requestedElement) {
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
    public MessageTemplate<ArtifactRequestMessage> artifactRequestMessageTemplate(final URI requestedArtifact) {
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
    public MessageTemplate<ContractRequestMessage> contractRequestMessageTemplate() {
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
    public MessageTemplate<UploadMessage> uploadMessageTemplate() {
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
    public MessageTemplate<ParticipantRequestMessage> participantRequestMessageTemplate(final URI requestedParticipant) {
        return () -> new ParticipantRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._requestedParticipant_(requestedParticipant)
                .build();

    }

    /**
     * Template for QueryMessage.
     *
     * @param queryLanguage the Language of the Query (e.g. SPARQL, SQL, XQUERY). See {@link QueryLanguage}
     * @param queryScope    the Scope of the Query (ALL connectors, ACTIVE connectors, INACTIVE connectors). See {@link QueryScope}
     * @param queryTarget   the type of IDS Components that are queried. See {@link QueryTarget}
     * @return template to build a {@link QueryMessage}
     */
    public MessageTemplate<QueryMessage> queryMessageTemplate(final QueryLanguage queryLanguage, final QueryScope queryScope, final QueryTarget queryTarget) {
        return () -> new QueryMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._queryLanguage_(queryLanguage)
                ._queryScope_(queryScope)
                ._recipientScope_(queryTarget)
                .build();
    }

    /**
     * Template for AccessTokenRequestMessage.
     *
     * @return template to build a {@link AccessTokenRequestMessage}
     */
    public MessageTemplate<AccessTokenRequestMessage> accessTokenRequestMessageTemplate() {
        return () -> new AccessTokenRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    /**
     * Template for AppRegistrationRequestMessage.
     *
     * @param affectedDataApp ID of affected data app
     * @return template to build a {@link AppRegistrationRequestMessage}
     */
    public MessageTemplate<AppRegistrationRequestMessage> appRegistrationRequestMessageTemplate(final URI affectedDataApp) {
        return () -> new AppRegistrationRequestMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedDataApp_(affectedDataApp)
                .build();
    }

    /**
     * Template for InvokeOperationMessage.
     *
     * @param operationReference reference of operation to execute by target connector
     * @return template to build a {@link InvokeOperationMessage}
     */
    public MessageTemplate<InvokeOperationMessage> invokeOperationMessageTemplate(final URI operationReference) {
        return () -> new InvokeOperationMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._operationReference_(operationReference)
                .build();
    }
}
