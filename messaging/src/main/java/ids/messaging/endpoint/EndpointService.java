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
package ids.messaging.endpoint;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Service for modifying the route of {@link MessageController} in a running application.
 */
@Slf4j
@Service
public class EndpointService {

    /**
     * The MessageController.
     */
    private MessageController messageController;

    /**
     * The RequestMappingHandlerMapping.
     */
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * Use /api/ids/data and /api/ids/infrastructure routes as default mappings.
     *
     * @param messageController The {@link MessageController} which will be mapped.
     * @param requestMappingHandlerMapping For managing Springs http route mappings.
     */
    @Autowired
    public EndpointService(final MessageController messageController,
                           final RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.messageController = messageController;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;

        if (log.isDebugEnabled()) {
            log.debug("Adding default mappings. [code=(IMSMED0063)]");
        }

        addMapping("/api/ids/data");
        addMapping("/api/ids/infrastructure");
    }

    /**
     * Add another endpoint to the MessageController.
     *
     * @param url The url for which a route to {@link MessageController} should be added.
     */
    public void addMapping(final String url) {
        if (log.isDebugEnabled()) {
            log.debug("Adding an endpoint mapping. [code=(IMSMED0064), endpoint=({})]", url);
        }

        final var requestMappingInfo = getRequestMappingInfo(url);

        try {
            requestMappingHandlerMapping
                    .registerMapping(requestMappingInfo, messageController,
                                     MessageController.class
                    .getDeclaredMethod("handleIDSMessage", HttpServletRequest.class));
        } catch (NoSuchMethodException e) {
            if (log.isErrorEnabled()) {
                log.error("MessageController could not be found for mapping route!"
                          + " [code=(IMSMEE0020)]");
            }
        }
    }

    /**
     * Remove an endpoint from the MessageController.
     *
     * @param url The url for which the {@link MessageController} should be unmapped for
     *            (RequestMappingInfo is deleted).
     */
    public void removeMapping(final String url) {
        if (log.isDebugEnabled()) {
            log.debug("Removing endpoint mapping. [code=(IMSMED0065), endpoint=({})]", url);
        }

        final var requestMappingInfo = getRequestMappingInfo(url);
        requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
    }

    private RequestMappingInfo getRequestMappingInfo(final String url) {
        return RequestMappingInfo
                .paths(url)
                .methods(RequestMethod.POST)
                .consumes(MediaType.MULTIPART_FORM_DATA_VALUE)
                .produces(MediaType.MULTIPART_FORM_DATA_VALUE)
                .build();
    }
}
