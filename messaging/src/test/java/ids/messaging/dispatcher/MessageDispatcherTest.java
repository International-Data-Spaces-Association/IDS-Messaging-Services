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
package ids.messaging.dispatcher;

import java.net.URI;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.NotificationMessage;
import de.fraunhofer.iais.eis.NotificationMessageBuilder;
import de.fraunhofer.iais.eis.NotificationMessageImpl;
import de.fraunhofer.iais.eis.RequestMessage;
import de.fraunhofer.iais.eis.RequestMessageBuilder;
import de.fraunhofer.iais.eis.RequestMessageImpl;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsPublicKeyProvider;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.core.daps.DapsValidator;
import ids.messaging.dispatcher.filter.PreDispatchingFilterResult;
import ids.messaging.dispatcher.testhandlers.NotificationMessageHandler;
import ids.messaging.handler.message.MessageHandler;
import ids.messaging.handler.request.RequestMessageHandlerService;
import ids.messaging.response.ErrorResponse;
import ids.messaging.util.IdsMessageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = { "shacl.validation=false" })
@ContextConfiguration(classes = {RequestMessageHandlerService.class, MessageDispatcherProvider.class, MessageDispatcherTest.TestContextConfiguration.class})
class MessageDispatcherTest {
    @Autowired
    private MessageDispatcherProvider messageDispatcherProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ids.messaging.handler.request.RequestMessageHandler requestMessageHandler;

    @MockBean
    private DapsPublicKeyProvider publicKeyProvider;

    @MockBean
    private ConfigContainer configurationContainer;

    @MockBean
    private DapsValidator dapsValidator;

    @MockBean
    private Connector connector;

    @MockBean
    private DapsTokenProvider provider;

    @MockBean
    private ConfigurationModel configurationModel;

    static class TestContextConfiguration {
        @Bean
        public Serializer getSerializer(){
            return new Serializer();
        }

        @Bean
        public ObjectMapper getMapper() { return new ObjectMapper(); }

        @Bean
        public MessageHandler<RequestMessageImpl> getRequestHandler() {return new ids.messaging.dispatcher.testhandlers.RequestMessageHandler();}

        @Bean
        public MessageHandler<NotificationMessageImpl> getNotificationHandler() {return new NotificationMessageHandler();}
    }

    @Test
    void testMessageDispatcher() throws Exception{
        Mockito.when(configurationContainer.getConnector()).thenReturn(connector);
        Mockito.when(connector.getId()).thenReturn(new URL("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5").toURI());
        Mockito.when(connector.getOutboundModelVersion()).thenReturn("1.0.3");
        Mockito.when(configurationContainer.getConfigurationModel()).thenReturn(configurationModel);
        Mockito.when(configurationModel.getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
        Mockito.when(provider.provideDapsToken()).thenReturn("Mocked Token.");
        Mockito.when(dapsValidator.checkDat(Mockito.any(DynamicAttributeToken.class), Mockito.anyMap())).thenReturn(true);

        final var dispatcher = messageDispatcherProvider.provideMessageDispatcher(objectMapper, requestMessageHandler, configurationContainer, dapsValidator);
        final var reqMsg = buildRequestMessage();
        final var notMsg = buildNotificationMessage();
        final var artMsg = buildArtifactRequestMessage();

        final var requestResponse = (ErrorResponse) dispatcher.process(reqMsg, null);
        assertEquals("request", requestResponse.getErrorMessage()); //use error message to check which handler got the message

        final var notificationResponse = (ErrorResponse) dispatcher.process(notMsg, null);
        assertEquals("Error while handling the request!", notificationResponse.getErrorMessage()); //use error message to check which handler got the message

        final var artifactResponse = (ErrorResponse) dispatcher.process(artMsg, null);
        assertEquals("No handler for provided message type was found!", artifactResponse.getErrorMessage());

        dispatcher.registerPreDispatchingAction(in -> PreDispatchingFilterResult.successResult());
        dispatcher.registerPreDispatchingAction(in -> PreDispatchingFilterResult.builder().withMessage("predispatching").withSuccess(false).build());
        assertEquals("predispatching", ((ErrorResponse) dispatcher.process(reqMsg, null)).getErrorMessage());
    }

    private NotificationMessage buildNotificationMessage() {
        final var now = IdsMessageUtils.getGregorianNow();
        return new NotificationMessageBuilder()
                ._issuerConnector_(URI.create("http://example.org#connector"))
                ._issued_(now)
                ._modelVersion_("4.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()
                                         ._tokenFormat_(TokenFormat.JWT)
                                         ._tokenValue_("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHQifQ.eyJ...")
                                         .build())
                ._senderAgent_(URI.create("http://example.org#senderAgent"))
                ._recipientConnector_(Util.asList(URI.create("http://example.org#recipientConnector1"), URI.create("http://example.org#recipientConnector2")))
                .build();

    }

    private RequestMessage buildRequestMessage() {
        final var now = IdsMessageUtils.getGregorianNow();
        return new RequestMessageBuilder()
                ._issuerConnector_(URI.create("http://example.org#connector"))
                ._issued_(now)
                ._modelVersion_("4.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()
                                         ._tokenFormat_(TokenFormat.JWT)
                                         ._tokenValue_("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHQifQ.eyJ...")
                                         .build())
                ._senderAgent_(URI.create("http://example.org#senderAgent"))
                ._recipientConnector_(Util.asList(URI.create("http://example.org#recipientConnector1"), URI.create("http://example.org#recipientConnector2")))
                .build();
    }

    private RequestMessage buildArtifactRequestMessage() {
        final var now = IdsMessageUtils.getGregorianNow();
        return new ArtifactRequestMessageBuilder()
                ._issuerConnector_(URI.create("http://example.org#connector"))
                ._issued_(now)
                ._requestedArtifact_(URI.create("http://example.artifact"))
                ._modelVersion_("4.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()
                        ._tokenFormat_(TokenFormat.JWT)
                        ._tokenValue_("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHQifQ.eyJ...")
                        .build())
                ._senderAgent_(URI.create("http://example.org#senderAgent"))
                ._recipientConnector_(Util.asList(URI.create("http://example.org#recipientConnector1"), URI.create("http://example.org#recipientConnector2")))
                .build();
    }


}
