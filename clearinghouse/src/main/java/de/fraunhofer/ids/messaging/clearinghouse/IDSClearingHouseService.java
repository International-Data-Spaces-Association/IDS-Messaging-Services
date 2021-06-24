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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;

/**
 * Interface for Communication with IDS ClearingHouses
 */
public interface IDSClearingHouseService {

    /**
     * Send a LogMessage with some random pid to ClearingHouse.
     *
     * @param messageToLog Infomodel Message that should be Logged
     * @return Response from ClearingHouse
     * @throws DapsTokenManagerException  if no DAT for sending the message could be received.
     * @throws URISyntaxException if Clearing House URI can not be parsed from String. Check Application Properties!
     * @throws IOException if message could not be sent or Serializer could not parse RDF to Java Object.
     * @throws ClaimsException if DAT of incoming message could not be validated.
     * @throws MultipartParseException if response could not be parsed to header and payload.
     */
    MessageProcessedNotificationMAP sendLogToClearingHouse(Message messageToLog) throws
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            URISyntaxException,
            IOException;


    /**
     * Send a LogMessage with given pid to ClearingHouse.
     *
     * @param messageToLog Infomodel Message that should be Logged
     * @param pid          process id under which the message will be logged
     * @return Response from ClearingHouse
     * @throws DapsTokenManagerException  if no DAT for sending the message could be received.
     * @throws URISyntaxException if Clearing House URI can not be parsed from String. Check Application Properties!
     * @throws IOException if message could not be sent or Serializer could not parse RDF to Java Object.
     * @throws ClaimsException if DAT of incoming message could not be validated.
     * @throws MultipartParseException if response could not be parsed to header and payload.
     */
    MessageProcessedNotificationMAP sendLogToClearingHouse(Message messageToLog, String pid)
            throws
            DapsTokenManagerException,
            URISyntaxException,
            IOException,
            ClaimsException,
            MultipartParseException;

    /**
     * Query the Clearing House.
     *
     * @param pid           process id to Query (or null when querying whole clearingHouse)
     * @param messageId     message id to Query (or null when querying whole process if pid is given)
     * @param queryLanguage Language of the Query
     * @param queryScope    Scope of the Query
     * @param queryTarget   Target of the Query
     * @param query         QueryString
     * @return Response from ClearingHouse
     * @throws DapsTokenManagerException  if no DAT for sending the message could be received.
     * @throws URISyntaxException if Clearing House URI can not be parsed from String. Check Application Properties!
     * @throws IOException if message could not be sent or Serializer could not parse RDF to Java Object.
     * @throws ClaimsException if DAT of incoming message could not be validated.
     * @throws MultipartParseException if response could not be parsed to header and payload.
     */
    ResultMAP queryClearingHouse(String pid, String messageId, QueryLanguage queryLanguage, QueryScope queryScope,
                                 QueryTarget queryTarget, String query)
            throws
            DapsTokenManagerException,
            URISyntaxException,
            ClaimsException,
            MultipartParseException,
            IOException;

}
