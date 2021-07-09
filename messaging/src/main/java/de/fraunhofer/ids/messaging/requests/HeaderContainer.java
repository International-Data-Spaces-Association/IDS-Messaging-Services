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

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Class containing IDS Header Fields
 */
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
