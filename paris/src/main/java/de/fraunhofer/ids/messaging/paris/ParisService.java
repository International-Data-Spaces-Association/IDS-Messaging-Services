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

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Participant;
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
import org.springframework.stereotype.Component;

/**
 * ParIS Communication Controller. Generates appropriate ids multipart messages
 * and sends them to the ParIS' infrastructure api.
 */
@Slf4j
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ParisService extends InfrastructureService
        implements IDSParisService {

    /**
     * The IdsRequestBuilderService.
     */
    IdsRequestBuilderService requestBuilderService;

    /**
     * @param container      the ConfigContainer
     * @param tokenProvider  the DapsTokenProvider
     * @param messageService the MessageService
     * @param idsRequestBuilderService service to send request messages
     */
    public ParisService(final ConfigContainer container,
                        final DapsTokenProvider tokenProvider,
                        final MessageService messageService,
                        final IdsRequestBuilderService idsRequestBuilderService) {
        super(container, tokenProvider, messageService, idsRequestBuilderService);
        this.requestBuilderService = idsRequestBuilderService;
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
        return requestBuilderService
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
        return requestBuilderService
                .newRequest()
                .subjectParticipant()
                .useMultipart()
                .operationDelete(participantUri)
                .execute(parisURI);
    }

    /**
     * {@inheritDoc}
     * @return
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
        return requestBuilderService
                .newRequest()
                .subjectDescription()
                .useMultipart()
                .operationGet(participantUri)
                .execute(parisURI);

    }
}
