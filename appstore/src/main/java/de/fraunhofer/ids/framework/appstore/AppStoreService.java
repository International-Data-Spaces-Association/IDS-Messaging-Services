package de.fraunhofer.ids.framework.appstore;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.InfrastructureService;
import de.fraunhofer.ids.messaging.requests.MessageContainer;
import de.fraunhofer.ids.messaging.requests.builder.IdsRequestBuilderService;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Generates  ids multipart messages and sends them to the App Store
 */
@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AppStoreService extends InfrastructureService
        implements IDSAppStoreService {

    IdsRequestBuilderService requestBuilderService;

    /**
     * Creates the IDSAppStore Communication controller.
     *
     * @param container      Configuration container
     * @param tokenProvider  providing DAT Token for RequestMessage
     * @param messageService providing Messaging functionality
     * @param idsRequestBuilderService service to send request messages
     */
    public AppStoreService(
            final ConfigContainer container,
            final DapsTokenProvider tokenProvider,
            final MessageService messageService,
            final IdsRequestBuilderService idsRequestBuilderService) {
        super(container, tokenProvider, messageService, idsRequestBuilderService);
        this.requestBuilderService = idsRequestBuilderService;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public MessageContainer<Object> requestAppStoreDescription( final URI appStoreURI )
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService.newRequest()
                                    .subjectDescription()
                                    .useMultipart()
                                    .operationGet(null)
                                    .execute(appStoreURI);
    }

    @Override
    public MessageContainer<Object> requestAppDescription( final URI appStoreURI, final URI app )
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService.newRequest()
                                    .subjectDescription()
                                    .useMultipart()
                                    .operationGet(null)
                                    .execute(appStoreURI);

    }

    @Override
    public MessageContainer<Object> requestAppArtifact( final URI appStoreURI, final URI app )
            throws
            IOException,
            DapsTokenManagerException,
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            SerializeException,
            UnknownResponseException,
            SendMessageException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        logBuildingHeader();
        return requestBuilderService.newRequest()
                                    .subjectArtifact()
                                    .useMultipart()
                                    .operationGet(app)
                                    .execute(appStoreURI);
    }
}
