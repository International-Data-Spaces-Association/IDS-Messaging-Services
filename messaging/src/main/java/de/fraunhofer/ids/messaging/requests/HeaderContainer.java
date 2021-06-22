package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Getter
public class HeaderContainer {

    private final Optional<Token> idsAuthorizationToken;
    private final DynamicAttributeToken idsSecurityToken;
    private final XMLGregorianCalendar idsIssued;
    private final URI idsCorrelationMessage;
    private final Optional<String> idsContentVersion;
    private final URI idsIssuerConnector;
    private final URI idsSenderAgent;
    private final List<URI> idsRecipientAgent;
    private final Optional<URI> idsTransferContract;
    private final String idsModelVersion;

}
