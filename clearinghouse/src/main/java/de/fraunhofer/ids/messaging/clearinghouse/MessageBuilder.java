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
package de.fraunhofer.ids.messaging.clearinghouse;

import java.net.URI;
import java.net.URISyntaxException;

import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.messaging.common.MessageBuilderException;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageBuilder {
    /**
     * @param configContainer the container holding the current configuration
     * @param clearingHouseUrl url of the clearing house
     * @param dapsTokenProvider the DAPS token provider
     * @return a LogMessage to be used as Header
     * @throws DapsTokenManagerException when {@link DapsTokenProvider} cannot get a Token
     * @throws URISyntaxException        when clearinghouse.url cannot be parsed as URI
     */
    public LogMessage buildLogMessage(final ConfigContainer configContainer,
                                      final DapsTokenProvider dapsTokenProvider,
                                      final String clearingHouseUrl)
            throws
            DapsTokenManagerException,
            URISyntaxException,
            MessageBuilderException {
        final var connector = configContainer.getConnector();

        try {
            return new LogMessageBuilder()
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._modelVersion_(connector.getOutboundModelVersion())
                    ._issuerConnector_(connector.getId())
                    ._senderAgent_(connector.getId())
                    ._securityToken_(dapsTokenProvider.getDAT())
                    ._recipientConnector_(
                            Util.asList(new URI(clearingHouseUrl)))
                    .build();
        } catch (ConstraintViolationException constraintViolationException) {
            throw new MessageBuilderException(constraintViolationException);
        }
    }

    /**
     * @param queryLanguage Language of the Query
     * @param queryScope    Scope of the Query
     * @param queryTarget   Target of the Query
     * @param configContainer the container holding the current configuration
     * @param dapsTokenProvider the DAPS token provider
     * @return built QueryMessage
     * @throws DapsTokenManagerException when {@link DapsTokenProvider} cannot get a Token
     */
    public QueryMessage buildQueryMessage(final QueryLanguage queryLanguage,
                                          final QueryScope queryScope,
                                          final QueryTarget queryTarget,
                                          final ConfigContainer configContainer,
                                          final DapsTokenProvider dapsTokenProvider)
            throws DapsTokenManagerException, MessageBuilderException {
        final var connector = configContainer.getConnector();

        try {
            return new QueryMessageBuilder()
                    ._securityToken_(dapsTokenProvider.getDAT())
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._modelVersion_(connector.getOutboundModelVersion())
                    ._issuerConnector_(connector.getId())
                    ._senderAgent_(connector.getId())
                    ._queryLanguage_(queryLanguage)
                    ._queryScope_(queryScope)
                    ._recipientScope_(queryTarget)
                    .build();
        } catch (ConstraintViolationException constraintViolationException) {
            throw new MessageBuilderException(constraintViolationException);
        }
    }
}
