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
package de.fraunhofer.ids.messaging.protocol;

import java.net.MalformedURLException;
import java.net.URI;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.messaging.common.SerializeException;
import okhttp3.Request;

public interface RequestBuilder {
    /**
     * @param message A Message from the IDS Infomation Model
     * @param target  An URI to which the message should be sent to
     * @return okhttp Response (to be updated to Infomation Model Message)
     * @throws SerializeException if serialization of Message Header is not successful.
     * @throws MalformedURLException Thrown to indicate that a malformed URL has occurred
     */
    Request build(Message message, URI target)
            throws MalformedURLException, SerializeException;


    /**
     * @param message A Message from the IDS Infomation Model
     * @param target  An URI to which the message should be sent to
     * @param payload The serialized payload to be sent with the Message
     * @return {@link Request}
     * @throws SerializeException if serialization of Message is not successful.
     * @throws MalformedURLException Thrown to indicate that a malformed URL has occurred
     */
    Request build(Message message, URI target, String payload)
            throws MalformedURLException, SerializeException;

}
