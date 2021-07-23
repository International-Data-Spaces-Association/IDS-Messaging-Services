/*
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
 */
package de.fraunhofer.ids.messaging.requests;

import java.net.URI;

import de.fraunhofer.iais.eis.AppAvailableMessage;
import de.fraunhofer.iais.eis.AppAvailableMessageBuilder;
import de.fraunhofer.iais.eis.AppDeleteMessage;
import de.fraunhofer.iais.eis.AppDeleteMessageBuilder;
import de.fraunhofer.iais.eis.AppUnavailableMessage;
import de.fraunhofer.iais.eis.AppUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorCertificateGrantedMessage;
import de.fraunhofer.iais.eis.ConnectorCertificateGrantedMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorCertificateRevokedMessage;
import de.fraunhofer.iais.eis.ConnectorCertificateRevokedMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import de.fraunhofer.iais.eis.ConnectorUpdateMessageBuilder;
import de.fraunhofer.iais.eis.ContractOfferMessage;
import de.fraunhofer.iais.eis.ContractOfferMessageBuilder;
import de.fraunhofer.iais.eis.ContractSupplementMessage;
import de.fraunhofer.iais.eis.ContractSupplementMessageBuilder;
import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantCertificateGrantedMessage;
import de.fraunhofer.iais.eis.ParticipantCertificateGrantedMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantCertificateRevokedMessage;
import de.fraunhofer.iais.eis.ParticipantCertificateRevokedMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantUnavailableMessage;
import de.fraunhofer.iais.eis.ParticipantUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantUpdateMessage;
import de.fraunhofer.iais.eis.ParticipantUpdateMessageBuilder;
import de.fraunhofer.iais.eis.RequestInProcessMessage;
import de.fraunhofer.iais.eis.RequestInProcessMessageBuilder;
import de.fraunhofer.iais.eis.ResourceUnavailableMessage;
import de.fraunhofer.iais.eis.ResourceUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ResourceUpdateMessage;
import de.fraunhofer.iais.eis.ResourceUpdateMessageBuilder;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class NotificationTemplateProvider {

    ConfigContainer container;

    DapsTokenProvider tokenProvider;

    public MessageTemplate<ConnectorUpdateMessage>
    connectorUpdateMessageTemplate(final URI affectedConnector) {
        return () -> new ConnectorUpdateMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedConnector_(affectedConnector)
                .build();
    }

    public MessageTemplate<ConnectorUnavailableMessage>
    connectorUnavailableMessageTemplate(final URI affectedConnector) {
        return () -> new ConnectorUnavailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedConnector_(affectedConnector)
                .build();
    }

    public MessageTemplate<ConnectorCertificateGrantedMessage>
    connectorCertificateGrantedMessageTemplate(final URI affectedConnector) {
        return () -> new ConnectorCertificateGrantedMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedConnector_(affectedConnector)
                .build();
    }

    public MessageTemplate<ConnectorCertificateRevokedMessage>
    connectorCertificateRevokedMessageTemplate(
            final URI affectedConnector,
            final TypedLiteral revocationReason) {
        return () -> new ConnectorCertificateRevokedMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedConnector_(affectedConnector)
                ._revocationReason_(revocationReason)
                .build();
    }

    public MessageTemplate<ParticipantUpdateMessage>
    participantUpdateMessageTemplate(final URI affectedParticipant) {
        return () -> new ParticipantUpdateMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedParticipant_(affectedParticipant)
                .build();
    }

    public MessageTemplate<ParticipantUnavailableMessage>
    participantUnavailableMessageTemplate(final URI affectedParticipant) {
        return () -> new ParticipantUnavailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedParticipant_(affectedParticipant)
                .build();
    }

    public MessageTemplate<ParticipantCertificateGrantedMessage>
    participantCertificateGrantedMessageTemplate(
            final URI affectedParticipant) {
        return () -> new ParticipantCertificateGrantedMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedParticipant_(affectedParticipant)
                .build();
    }

    public MessageTemplate<ParticipantCertificateRevokedMessage>
    participantCertificateRevokedMessageTemplate(
            final URI affectedParticipant,
            final TypedLiteral revocationReason) {
        return () -> new ParticipantCertificateRevokedMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedParticipant_(affectedParticipant)
                ._revocationReason_(revocationReason)
                .build();
    }

    public MessageTemplate<AppAvailableMessage>
    appAvailableMessageTemplate(final URI affectedApp) {
        return () -> new AppAvailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedApp)
                .build();
    }

    public MessageTemplate<AppUnavailableMessage>
    appUnavailableMessageTemplate(final URI affectedApp) {
        return () -> new AppUnavailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedApp)
                .build();
    }

    public MessageTemplate<AppDeleteMessage>
    appDeleteMessageTemplate(final URI affectedApp) {
        return () -> new AppDeleteMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedApp)
                .build();
    }

    public MessageTemplate<ResourceUpdateMessage>
    resourceUpdateMessageTemplate(final URI affectedResource) {
        return () -> new ResourceUpdateMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedResource)
                .build();
    }

    public MessageTemplate<ResourceUnavailableMessage>
    resourceUnavailableMessageTemplate(final URI affectedResource) {
        return () -> new ResourceUnavailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedResource)
                .build();
    }

    public MessageTemplate<ContractOfferMessage>
    contractOfferMessageTemplate() {
        return () -> new ContractOfferMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public MessageTemplate<ContractSupplementMessage>
    contractSupplementMessageTemplate() {
        return () -> new ContractSupplementMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public MessageTemplate<RequestInProcessMessage>
    requestInProcessMessageTemplate() {
        return () -> new RequestInProcessMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public MessageTemplate<MessageProcessedNotificationMessage>
    messageProcessedNotificationMessageTemplate() {
        return () -> new MessageProcessedNotificationMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public MessageTemplate<LogMessage>
    logMessageTemplate(final URI clearingHouseUrl) {
        return () -> new LogMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector()
                                         .getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._recipientConnector_(
                        Util.asList(clearingHouseUrl))
                .build();
    }
}
