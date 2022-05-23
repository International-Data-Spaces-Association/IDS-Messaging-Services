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
package ids.messaging.appstore;

import java.io.IOException;
import java.net.URI;

import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.InfrastructureService;
import ids.messaging.requests.MessageContainer;
import ids.messaging.requests.builder.IdsRequestBuilderService;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Generates  ids multipart messages and sends them to the App Store.
 */
@Slf4j
@Service
public class AppStoreService extends InfrastructureService
        implements IDSAppStoreService {

    /**
     * The IdsRequestBuilderService.
     */
    private final IdsRequestBuilderService idsRequestBuilderService;

    /**
     * Creates the IDSAppStore Communication controller.
     *
     * @param container Configuration container.
     * @param tokenProvider Providing DAT Token for RequestMessage.
     * @param messageService Providing Messaging functionality.
     * @param idsRequestBuilderService Service to send request messages.
     */
    public AppStoreService(
            final ConfigContainer container,
            final DapsTokenProvider tokenProvider,
            final MessageService messageService,
            final IdsRequestBuilderService idsRequestBuilderService) {
        super(container, tokenProvider, messageService, idsRequestBuilderService);
        this.idsRequestBuilderService = idsRequestBuilderService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<Object> requestAppStoreDescription(final URI appStoreURI)
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
        return idsRequestBuilderService.newRequest()
                                       .subjectDescription()
                                       .useMultipart()
                                       .operationGet(null)
                                       .execute(appStoreURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<Object> requestAppDescription(final URI appStoreURI, final URI app)
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
        return idsRequestBuilderService.newRequest()
                                       .subjectDescription()
                                       .useMultipart()
                                       .operationGet(app)
                                       .execute(appStoreURI);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<Object> requestAppArtifact(final URI appStoreURI, final URI app)
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
        return idsRequestBuilderService.newRequest()
                                       .subjectArtifact()
                                       .useMultipart()
                                       .operationGet(app)
                                       .execute(appStoreURI);
    }
}
