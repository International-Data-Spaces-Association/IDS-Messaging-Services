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

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Class containing IDS Header Fields.
 */
@AllArgsConstructor
@Getter
public class HeaderContainer {
    /**
     * IDS Header field: AuthorizationToken.
     */
    private final Optional<Token> idsAuthorizationToken;

    /**
     * IDS Header field: SecurityToken.
     */
    private final DynamicAttributeToken idsSecurityToken;

    /**
     * IDS Header field: Issued at.
     */
    private final XMLGregorianCalendar idsIssued;

    /**
     * IDS Header field: CorrelationMessage.
     */
    private final URI idsCorrelationMessage;

    /**
     * IDS Header field: ContentVersion.
     */
    private final Optional<String> idsContentVersion;

    /**
     * IDS Header field: IssuerConnector.
     */
    private final URI idsIssuerConnector;

    /**
     * IDS Header field: SenderAgent.
     */
    private final URI idsSenderAgent;

    /**
     * IDS Header field: RecipientAgent.
     */
    private final List<URI> idsRecipientAgent;

    /**
     * IDS Header field: TransferContract.
     */
    private final Optional<URI> idsTransferContract;

    /**
     * IDS Header field: ModelVersion.
     */
    private final String idsModelVersion;
}
