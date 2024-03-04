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
package ids.messaging.handler.message;

import de.fraunhofer.iais.eis.Message;
import ids.messaging.response.MessageResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * MessageHandler, also passing DAT Claims for additional checks. Supports files in payload.
 *
 * @param <T> Type of Message accepted by handler
 */
public interface MessageFilesAndClaimsHandler<T extends Message> extends MessageAndClaimsHandler<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    default MessageResponse handleMessage(T queryHeader, MessagePayload payload, Optional<Jws<Claims>> optionalClaimsJws)
            throws MessageHandlerException {
        return handleMessage(queryHeader, payload, optionalClaimsJws, null);
    }

    MessageResponse handleMessage(T queryHeader, MessagePayload payload,
                                  Optional<Jws<Claims>> optionalClaimsJws, MultiValueMap<String, MultipartFile> files)
            throws MessageHandlerException;


}
