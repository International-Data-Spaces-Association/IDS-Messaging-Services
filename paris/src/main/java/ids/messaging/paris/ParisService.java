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
package ids.messaging.paris;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Participant;
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
import org.springframework.stereotype.Component;

/**
 * ParIS Communication Controller. Generates appropriate ids multipart messages
 * and sends them to the ParIS' infrastructure api.
 */
@Slf4j
@Component
public class ParisService extends InfrastructureService
        implements IDSParisService {

    /**
     * The IdsRequestBuilderService.
     */
    private final IdsRequestBuilderService idsRequestBuilderService;

    /**
     * ParisService constructor.
     *
     * @param container The ConfigContainer.
     * @param tokenProvider The DapsTokenProvider.
     * @param messageService The MessageService.
     * @param idsRequestBuilderService Service to send request messages.
     */
    public ParisService(final ConfigContainer container,
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
    public MessageContainer<?> updateParticipantAtParIS(
            final URI parisURI, final Participant participant)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException,
            ShaclValidatorException,
            SerializeException,
            RejectionException,
            UnknownResponseException,
            SendMessageException,
            UnexpectedPayloadException,
            DeserializeException {
        logBuildingHeader();
        return idsRequestBuilderService
                .newRequest()
                .withPayload(participant)
                .subjectParticipant()
                .useMultipart()
                .operationUpdate(participant.getId())
                .execute(parisURI);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<?> unregisterAtParIS(
            final URI parisURI, final URI participantUri)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException,
            ShaclValidatorException,
            SerializeException,
            RejectionException,
            UnknownResponseException,
            SendMessageException,
            UnexpectedPayloadException,
            DeserializeException {
        logBuildingHeader();
        return idsRequestBuilderService
                .newRequest()
                .subjectParticipant()
                .useMultipart()
                .operationDelete(participantUri)
                .execute(parisURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<Object> requestParticipant(
            final URI parisURI, final URI participantUri)
            throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            IOException,
            ShaclValidatorException,
            SerializeException,
            RejectionException,
            UnknownResponseException,
            SendMessageException,
            UnexpectedPayloadException,
            DeserializeException {
        logBuildingHeader();
        return idsRequestBuilderService
                .newRequest()
                .subjectDescription()
                .useMultipart()
                .operationGet(participantUri)
                .execute(parisURI);

    }
}
