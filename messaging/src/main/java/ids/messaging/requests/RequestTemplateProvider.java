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
package ids.messaging.requests;

import java.net.URI;

import de.fraunhofer.iais.eis.AccessTokenRequestMessage;
import de.fraunhofer.iais.eis.AccessTokenRequestMessageBuilder;
import de.fraunhofer.iais.eis.AppRegistrationRequestMessage;
import de.fraunhofer.iais.eis.AppRegistrationRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.ContractRequestMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.InvokeOperationMessage;
import de.fraunhofer.iais.eis.InvokeOperationMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantRequestMessage;
import de.fraunhofer.iais.eis.ParticipantRequestMessageBuilder;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.RequestMessageBuilder;
import de.fraunhofer.iais.eis.UploadMessage;
import de.fraunhofer.iais.eis.UploadMessageBuilder;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.util.IdsMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Templates for request messages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestTemplateProvider {

    /**
     * The ConfigContainer.
     */
    private final ConfigContainer container;

    /**
     * The DapsTokenProvider.
     */
    private final DapsTokenProvider tokenProvider;

    /**
     * Template for DescriptionRequestMessages.
     *
     * @param requestedElement Requested element ID, or null if selfdescription is requested.
     * @return Template to build a {@link DescriptionRequestMessage}.
     */
    public MessageTemplate<DescriptionRequestMessage> descriptionRequestMessageTemplate(
            final URI requestedElement) {
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
     * @param requestedArtifact ID of requested artifact.
     * @return Template to build a {@link ArtifactRequestMessage}.
     */
    public MessageTemplate<ArtifactRequestMessage> artifactRequestMessageTemplate(
            final URI requestedArtifact) {
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
     * @return Template to build a {@link ContractRequestMessage}.
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
     * Template for RequestMessage.
     *
     * @return Template to build a {@link RequestMessage}.
     */
    public MessageTemplate<RequestMessage> requestMessageTemplate() {
        return () -> new RequestMessageBuilder()
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
     * @return Template to build an {@link UploadMessage}.
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
     * @param requestedParticipant ID of requested participant.
     * @return Template to build a {@link ParticipantRequestMessage}.
     */
    public MessageTemplate<ParticipantRequestMessage> participantRequestMessageTemplate(
            final URI requestedParticipant) {
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
     * @param queryLanguage The Language of the Query (e.g. SPARQL, SQL, XQUERY).
     *                      See {@link QueryLanguage}-
     * @param queryScope The Scope of the Query (ALL connectors, ACTIVE connectors, INACTIVE
     *                   connectors). See {@link QueryScope}.
     * @param queryTarget The type of IDS Components that are queried. See {@link QueryTarget}.
     * @return Template to build a {@link QueryMessage}.
     */
    public MessageTemplate<QueryMessage> queryMessageTemplate(final QueryLanguage queryLanguage,
                                                              final QueryScope queryScope,
                                                              final QueryTarget queryTarget) {
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
     * @return Template to build a {@link AccessTokenRequestMessage}.
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
     * @param affectedDataApp ID of affected data app.
     * @return Template to build a {@link AppRegistrationRequestMessage}.
     */
    public MessageTemplate<AppRegistrationRequestMessage> appRegistrationRequestMessageTemplate(
            final URI affectedDataApp) {
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
     * @param operationReference Reference of operation to execute by target connector.
     * @return Template to build a {@link InvokeOperationMessage}.
     */
    public MessageTemplate<InvokeOperationMessage> invokeOperationMessageTemplate(
            final URI operationReference) {
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
