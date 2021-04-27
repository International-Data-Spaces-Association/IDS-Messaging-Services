package de.fraunhofer.ids.framework.clearinghouse;

import java.net.URI;
import java.net.URISyntaxException;

import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.DapsTokenManagerException;
import de.fraunhofer.ids.framework.daps.DapsTokenProvider;
import de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils;
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
            throws DapsTokenManagerException, URISyntaxException {
        final var connector = configContainer.getConnector();

        return new LogMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._senderAgent_(connector.getId())
                ._securityToken_(dapsTokenProvider.getDAT())
                ._recipientConnector_(Util.asList(new URI(clearingHouseUrl)))
                .build();
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
            throws DapsTokenManagerException {
        final var connector = configContainer.getConnector();

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
    }
}
