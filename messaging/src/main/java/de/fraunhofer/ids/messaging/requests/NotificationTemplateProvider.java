package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
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
public class NotificationTemplateProvider {

    ConfigContainer container;
    DapsTokenProvider tokenProvider;

    public MessageTemplate<ConnectorUpdateMessage> connectorUpdateMessageTemplate(final URI affectedConnector){
        return () -> new ConnectorUpdateMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedConnector_(affectedConnector)
                .build();
    }

    public MessageTemplate<ConnectorUnavailableMessage> connectorUnavailableMessageTemplate(final URI affectedConnector){
        return () -> new ConnectorUnavailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedConnector_(affectedConnector)
                .build();
    }

    public MessageTemplate<ConnectorCertificateGrantedMessage> connectorCertificateGrantedMessageTemplate(final URI affectedConnector){
        return () -> new ConnectorCertificateGrantedMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedConnector_(affectedConnector)
                .build();
    }

    public MessageTemplate<ConnectorCertificateRevokedMessage> connectorCertificateRevokedMessageTemplate(final URI affectedConnector, final TypedLiteral revocationReason){
        return () -> new ConnectorCertificateRevokedMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedConnector_(affectedConnector)
                ._revocationReason_(revocationReason)
                .build();
    }

    public MessageTemplate<ParticipantUpdateMessage> participantUpdateMessageTemplate(final URI affectedParticipant){
        return () -> new ParticipantUpdateMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedParticipant_(affectedParticipant)
                .build();
    }

    public MessageTemplate<ParticipantUnavailableMessage> participantUnavailableMessageTemplate(final URI affectedParticipant){
        return () -> new ParticipantUnavailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedParticipant_(affectedParticipant)
                .build();
    }

    public MessageTemplate<ParticipantCertificateGrantedMessage> participantCertificateGrantedMessageTemplate(final URI affectedParticipant){
        return () -> new ParticipantCertificateGrantedMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedParticipant_(affectedParticipant)
                .build();
    }

    public MessageTemplate<ParticipantCertificateRevokedMessage> participantCertificateRevokedMessageTemplate(final URI affectedParticipant, final TypedLiteral revocationReason){
        return () -> new ParticipantCertificateRevokedMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedParticipant_(affectedParticipant)
                ._revocationReason_(revocationReason)
                .build();
    }

    public MessageTemplate<AppAvailableMessage> appAvailableMessageTemplate(final URI affectedApp){
        return () -> new AppAvailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedApp)
                .build();
    }

    public MessageTemplate<AppUnavailableMessage> appUnavailableMessageTemplate(final URI affectedApp){
        return () -> new AppUnavailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedApp)
                .build();
    }

    public MessageTemplate<AppDeleteMessage> appDeleteMessageTemplate(final URI affectedApp){
        return () -> new AppDeleteMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedApp)
                .build();
    }

    public MessageTemplate<ResourceUpdateMessage> resourceUpdateMessageTemplate(final URI affectedResource){
        return () -> new ResourceUpdateMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedResource)
                .build();
    }

    public MessageTemplate<ResourceUnavailableMessage> resourceUnavailableMessageTemplate(final URI affectedResource){
        return () -> new ResourceUnavailableMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                ._affectedResource_(affectedResource)
                .build();
    }

    public MessageTemplate<ContractOfferMessage> contractOfferMessageTemplate(){
        return () -> new ContractOfferMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public MessageTemplate<ContractSupplementMessage> contractSupplementMessageTemplate(){
        return () -> new ContractSupplementMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public MessageTemplate<RequestInProcessMessage> requestInProcessMessageTemplate(){
        return () -> new RequestInProcessMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public MessageTemplate<MessageProcessedNotificationMessage> messageProcessedNotificationMessageTemplate(){
        return () -> new MessageProcessedNotificationMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }

    public MessageTemplate<LogMessage> logMessageTemplate(){
        return () -> new LogMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(container.getConnector().getOutboundModelVersion())
                ._issuerConnector_(container.getConnector().getId())
                ._senderAgent_(container.getConnector().getId())
                ._securityToken_(tokenProvider.getDAT())
                .build();
    }
}
