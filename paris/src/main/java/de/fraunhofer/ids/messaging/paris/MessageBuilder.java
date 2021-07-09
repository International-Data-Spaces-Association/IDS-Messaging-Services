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
package de.fraunhofer.ids.messaging.paris;

import java.net.URI;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageBuilder {
    public static ParticipantUpdateMessage buildParticipantUpdateMessage( final DynamicAttributeToken securityToken, final Connector connector, final URI participantURI ) {
        return new ParticipantUpdateMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._securityToken_(securityToken)
                ._affectedParticipant_(participantURI)
                ._senderAgent_(connector.getId())
                .build();
    }

    public static ParticipantRequestMessage buildParticipantRequestMessage( final DynamicAttributeToken securityToken, final Connector connector, final URI participant) {
        return new ParticipantRequestMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._securityToken_(securityToken)
                ._requestedParticipant_(participant)
                ._senderAgent_(connector.getId())
                .build();
    }

    public static ParticipantUnavailableMessage buildParticipantUnavailableMessage( final DynamicAttributeToken securityToken, final Connector connector, final URI participant) {
        return new ParticipantUnavailableMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._securityToken_(securityToken)
                ._affectedParticipant_(participant)
                ._senderAgent_(connector.getId())
                .build();
    }


}
