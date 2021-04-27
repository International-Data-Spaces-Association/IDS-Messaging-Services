package de.fraunhofer.ids.framework.messaging.endpoint;

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

    private MessageController            messageController;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * Use <code>/api/ids/data</code> and <code>/api/ids/infrastructure</code> routes as default mappings.
     *
     * @param messageController            the {@link MessageController} which will be mapped
     * @param requestMappingHandlerMapping for managing Springs http route mappings
     */
    @Autowired
    public EndpointService(final MessageController messageController,
                           final RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.messageController = messageController;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;

        if (log.isDebugEnabled()) {
            log.debug("Adding default mappings");
        }

        addMapping("/api/ids/data");
        addMapping("/api/ids/infrastructure");
    }

    /**
     * Add another endpoint to the MessageController.
     *
     * @param url the url for which a route to {@link MessageController} should be added
     */
    public void addMapping(final String url) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Adding a mapping for url %s", url));
        }

        final var requestMappingInfo = getRequestMappingInfo(url);

        try {
            requestMappingHandlerMapping.registerMapping(requestMappingInfo, messageController, MessageController.class
                    .getDeclaredMethod("handleIDSMessage", HttpServletRequest.class));
        } catch (NoSuchMethodException e) {
            if (log.isDebugEnabled()) {
                log.error("MessageController could not be found for mapping route!");
            }
        }
    }

    /**
     * Remove an endpoint from the MessageController.
     *
     * @param url the url for which the {@link MessageController} should be unmapped for (RequestMappingInfo is deleted)
     */
    public void removeMapping(final String url) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Remove mapping for url %s", url));
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
