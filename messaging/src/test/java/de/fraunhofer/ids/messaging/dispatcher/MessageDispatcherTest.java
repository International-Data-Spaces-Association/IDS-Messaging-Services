package de.fraunhofer.ids.messaging.dispatcher;

import java.net.URI;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.DapsPublicKeyProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsValidator;
import de.fraunhofer.ids.messaging.dispatcher.testhandlers.NotificationMessageHandler;
import de.fraunhofer.ids.messaging.dispatcher.testhandlers.RequestMessageHandler;
import de.fraunhofer.ids.messaging.handler.message.MessageHandler;
import de.fraunhofer.ids.messaging.handler.request.RequestMessageHandlerService;
import de.fraunhofer.ids.messaging.response.ErrorResponse;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@FieldDefaults(level = AccessLevel.PRIVATE)
@ContextConfiguration(classes = {RequestMessageHandlerService.class, MessageDispatcherProvider.class, MessageDispatcherTest.TestContextConfiguration.class})
@TestPropertySource(properties = { "shacl.validation=false" })
class MessageDispatcherTest {
    @Autowired
    MessageDispatcherProvider messageDispatcherProvider;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    de.fraunhofer.ids.messaging.handler.request.RequestMessageHandler requestMessageHandler;

    @MockBean
    DapsPublicKeyProvider publicKeyProvider;

    @MockBean
    ConfigContainer configurationContainer;

    @MockBean
    DapsValidator dapsValidator;

    @MockBean
    Connector connector;

    @MockBean
    DapsTokenProvider provider;

    @MockBean
    ConfigurationModel configurationModel;

    static class TestContextConfiguration {
        @Bean
        public Serializer getSerializer(){
            return new Serializer();
        }

        @Bean
        public ObjectMapper getMapper() { return new ObjectMapper(); }

        @Bean
        public MessageHandler<RequestMessageImpl> getRequestHandler() {return new RequestMessageHandler();}

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
        Mockito.when(publicKeyProvider.providePublicKeys()).thenReturn(null);
        Mockito.when(dapsValidator.checkDat(Mockito.any(DynamicAttributeToken.class), Mockito.anyMap())).thenReturn(true);

        final var dispatcher = messageDispatcherProvider.provideMessageDispatcher(objectMapper, requestMessageHandler, configurationContainer, dapsValidator);
        final var reqMsg = buildRequestMessage();
        final var notMsg = buildNotificationMessage();

        final var requestResponse = (ErrorResponse) dispatcher.process(reqMsg, null);
        assertEquals("request", requestResponse.getErrorMessage()); //use error message to check which handler got the message

        final var notificationResponse = (ErrorResponse) dispatcher.process(notMsg, null);
        assertEquals("notification", notificationResponse.getErrorMessage()); //use error message to check which handler got the message
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


}
