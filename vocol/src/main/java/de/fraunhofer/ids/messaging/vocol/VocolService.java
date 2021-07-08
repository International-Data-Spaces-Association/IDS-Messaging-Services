package de.fraunhofer.ids.messaging.vocol;

import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.requests.QueryService;
import de.fraunhofer.ids.messaging.requests.builder.IdsRequestBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VocolService extends QueryService {
    IdsRequestBuilderService requestBuilderService;
    /**
     * QueryService constructor.
     *
     * @param container      the ConfigContainer
     * @param tokenProvider  the DapsTokenProvider
     * @param messageService the MessageService
     */
    public VocolService(
            ConfigContainer container,
            DapsTokenProvider tokenProvider,
            MessageService messageService,
            IdsRequestBuilderService idsRequestBuilderService ) {
        super(container, tokenProvider, messageService, idsRequestBuilderService);
        this.requestBuilderService = idsRequestBuilderService;
    }
}
