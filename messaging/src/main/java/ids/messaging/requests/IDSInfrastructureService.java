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
package ids.messaging.requests;

import java.io.IOException;
import java.net.URI;

import ids.messaging.common.DeserializeException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.protocol.http.SendMessageException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.requests.exceptions.RejectionException;
import ids.messaging.requests.exceptions.UnexpectedPayloadException;
import lombok.NonNull;

/**
 * Interface for building selfdescription requests.
 */
public interface IDSInfrastructureService {
    /**
     * Interface Method to create a request for a self-description and send it.
     *
     * @param uri The target URI.
     * @return MessageContrainer with the response.
     * @throws IOException Every other exception.
     * @throws DapsTokenManagerException DAPS Token can not be acquired.
     * @throws MultipartParseException Response could not be parsed.
     * @throws ClaimsException Errors occurred while validating a DAT token.
     * @throws ShaclValidatorException Response did not pass SHACL Validation.
     * @throws SerializeException Serializing using the IDS-Serializer threw an IOException.
     * @throws UnknownResponseException Could indicate a new unknown IDS-Message-Type.
     * @throws SendMessageException Sending the IDS-Request returned an IOException.
     * @throws DeserializeException Deserializing the response threw an IOException.
     * @throws RejectionException Response was a RejectionMessage.
     * @throws UnexpectedPayloadException Payload did not match the expected format.
     */
    MessageContainer<?> requestSelfDescription(@NonNull URI uri)
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
            UnexpectedPayloadException;

    /**
     * Interface Method to create a request for a self-description and send it.
     *
     * @param uri The target URI.
     * @param requestedElement URI of the requested element.
     * @return MessageContrainer with the response.
     * @throws IOException Every other exception.
     * @throws DapsTokenManagerException DAPS Token can not be acquired.
     * @throws MultipartParseException Response could not be parsed.
     * @throws ClaimsException Errors occurred while validating a DAT token.
     * @throws ShaclValidatorException Response did not pass SHACL Validation.
     * @throws SerializeException Serializing using the IDS-Serializer threw an IOException.
     * @throws UnknownResponseException Could indicate a new unknown IDS-Message-Type.
     * @throws SendMessageException Sending the IDS-Request returned an IOException.
     * @throws DeserializeException Deserializing the response threw an IOException.
     * @throws RejectionException Response was a RejectionMessage.
     * @throws UnexpectedPayloadException Payload did not match the expected format.
     */
    MessageContainer<?> requestSelfDescription(@NonNull URI uri,
                                               @NonNull URI requestedElement)
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
            UnexpectedPayloadException;
}
