package de.fraunhofer.ids.framework.clearinghouse;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.DapsTokenManagerException;
import de.fraunhofer.ids.framework.daps.DapsTokenProvider;

import java.net.URI;
import java.net.URISyntaxException;

import static de.fraunhofer.ids.framework.messaging.util.IdsMessageUtils.getGregorianNow;

public class MessageBuilder {
    /**
     * Utility classes (only static methods and fields) do not have a public constructor.
     * Instantiating them does not make sense, prevent instantiating.
     */
    protected MessageBuilder() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return a LogMessage to be used as Header
     *
     * @throws DapsTokenManagerException when {@link DapsTokenProvider} cannot get a Token
     * @throws URISyntaxException        when clearinghouse.url cannot be parsed as URI
     */
    public static LogMessage buildLogMessage( final ConfigContainer configContainer,
                                              final DapsTokenProvider dapsTokenProvider,
                                              final String clearingHouseUrl )
            throws DapsTokenManagerException, URISyntaxException {
        var connector = configContainer.getConnector();
        return new LogMessageBuilder()
                ._issued_(getGregorianNow())
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
     *
     * @return built QueryMessage
     *
     * @throws DapsTokenManagerException when {@link DapsTokenProvider} cannot get a Token
     */
    public static QueryMessage buildQueryMessage( final QueryLanguage queryLanguage,
                                                  final QueryScope queryScope,
                                                  final QueryTarget queryTarget,
                                                  final ConfigContainer configContainer,
                                                  final DapsTokenProvider dapsTokenProvider,
                                                  final String clearingHouseUrl ) throws DapsTokenManagerException {
        var connector = configContainer.getConnector();
        return new QueryMessageBuilder()
                ._securityToken_(dapsTokenProvider.getDAT())
                ._issued_(getGregorianNow())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._issuerConnector_(connector.getId())
                ._senderAgent_(connector.getId())
                ._queryLanguage_(queryLanguage)
                ._queryScope_(queryScope)
                ._recipientScope_(queryTarget)
                .build();
    }
}
