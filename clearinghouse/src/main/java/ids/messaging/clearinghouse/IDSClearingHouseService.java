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
package ids.messaging.clearinghouse;

import java.io.IOException;
import java.net.URISyntaxException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import ids.messaging.common.DeserializeException;
import ids.messaging.common.MessageBuilderException;
import ids.messaging.common.SerializeException;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.protocol.UnexpectedResponseException;
import ids.messaging.protocol.http.ShaclValidatorException;
import ids.messaging.protocol.multipart.UnknownResponseException;
import ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import ids.messaging.protocol.multipart.mapping.ResultMAP;
import ids.messaging.protocol.multipart.parser.MultipartParseException;

/**
 * Interface for Communication with IDS ClearingHouses.
 */
public interface IDSClearingHouseService {

    /**
     * Send a LogMessage with given pid to ClearingHouse.
     *
     * @param messageToLog Infomodel Message that should be Logged.
     * @param pid Process id under which the message will be logged.
     * @return Response from ClearingHouse.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws URISyntaxException If Clearing House URI can not be parsed from String.
     * Check Application Properties!
     * @throws IOException If message could not be sent or Serializer could not parse
     * RDF to Java Object.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws ClaimsException Exception while validating the DAT from the Broker Response.
     * @throws UnknownResponseException Thrown during converting IDS-Response into a corresponding
     * Object if no possible cast found.
     * @throws DeserializeException Exception that is thrown if deserializing a message threw
     * an IOException.
     * @throws UnexpectedResponseException Exception that is thrown if the received response-type
     * is not expected as a response to the request send.
     * @throws SerializeException Exception is thrown if serializing a message threw an IOException.
     * @throws ShaclValidatorException SHACL-Validation, received message header does not conform
     * to IDS-Infomodel and did not pass SHACL-Validation.
     * @throws MessageBuilderException Exception that is thrown if building an IDS-Message
     * with the given information threw a  RuntimeException.
     */
    MessageProcessedNotificationMAP sendLogToClearingHouse(Message messageToLog,
                                                           String pid)
            throws
            DapsTokenManagerException,
            URISyntaxException,
            IOException,
            ClaimsException,
            MultipartParseException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            ShaclValidatorException,
            SerializeException,
            MessageBuilderException;

    /**
     * Query the Clearing House.
     *
     * @param pid Process id to Query (or null when querying whole clearingHouse).
     * @param messageId Message id to Query (or null when querying whole process if pid is given).
     * @param queryLanguage Language of the Query.
     * @param queryScope Scope of the Query.
     * @param queryTarget Target of the Query.
     * @param query QueryString.
     * @return Response from ClearingHouse.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws URISyntaxException If Clearing House URI can not be parsed from String.
     * Check Application Properties!
     * @throws IOException If message could not be sent or Serializer could not parse
     * RDF to Java Object.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws ClaimsException Exception while validating the DAT from the Broker Response.
     * @throws UnknownResponseException Thrown during converting IDS-Response into a
     * corresponding Object if no possible cast found.
     * @throws DeserializeException Exception that is thrown if deserializing a message
     * threw an IOException.
     * @throws UnexpectedResponseException Exception that is thrown if the received
     * response-type is not expected as a response to the request send.
     * @throws SerializeException Exception is thrown if serializing a message threw an IOException.
     * @throws ShaclValidatorException SHACL-Validation, received message header does not
     * conform to IDS-Infomodel and did not pass SHACL-Validation.
     * @throws MessageBuilderException Exception that is thrown if building an IDS-Message with
     * the given information threw a RuntimeException.
     */
    ResultMAP queryClearingHouse(String pid,
                                 String messageId,
                                 QueryLanguage queryLanguage,
                                 QueryScope queryScope,
                                 QueryTarget queryTarget,
                                 String query)
            throws
            DapsTokenManagerException,
            URISyntaxException,
            ClaimsException,
            MultipartParseException,
            IOException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            ShaclValidatorException,
            SerializeException,
            MessageBuilderException;

    /**
     * Register a pid at the clearinghouse for logging usage.
     *
     * @param pid Pid to register.
     * @param connectorIDs IDs the pid is registered for.
     * @return Response from clearing house.
     * @throws DapsTokenManagerException If no DAT for sending the message could be received.
     * @throws URISyntaxException If Clearing House URI can not be parsed from String.
     * Check Application Properties!
     * @throws IOException If message could not be sent or Serializer could not parse
     * RDF to Java Object.
     * @throws ClaimsException If DAT of incoming message could not be validated.
     * @throws MultipartParseException If response could not be parsed to header and payload.
     * @throws ClaimsException Exception while validating the DAT from the Broker Response.
     * @throws UnknownResponseException Thrown during converting IDS-Response into a
     * corresponding Object if no possible cast found.
     * @throws DeserializeException Exception that is thrown if deserializing a message
     * threw an IOException.
     * @throws UnexpectedResponseException Exception that is thrown if the received
     * response-type is not expected as a response to the request send.
     * @throws SerializeException Exception is thrown if serializing a message threw an IOException.
     * @throws ShaclValidatorException SHACL-Validation, received message header does not
     * conform to IDS-Infomodel and did not pass SHACL-Validation.
     * @throws MessageBuilderException Exception that is thrown if building an IDS-Message with
     * the given information threw a RuntimeException.
     */
    MessageProcessedNotificationMAP registerPidAtClearingHouse(String pid,
                                                               String... connectorIDs)
            throws
            DapsTokenManagerException,
            URISyntaxException,
            ClaimsException,
            MultipartParseException,
            IOException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            ShaclValidatorException,
            SerializeException,
            MessageBuilderException;
}
