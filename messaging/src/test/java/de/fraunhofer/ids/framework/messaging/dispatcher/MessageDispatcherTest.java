package de.fraunhofer.ids.framework.messaging.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.DapsPublicKeyProvider;
import de.fraunhofer.ids.framework.daps.DapsTokenProvider;
import de.fraunhofer.ids.framework.daps.DapsValidator;
import de.fraunhofer.ids.framework.messaging.dispatcher.testhandlers.NotificationMessageHandler;
import de.fraunhofer.ids.framework.messaging.dispatcher.testhandlers.RequestMessageHandler;
import de.fraunhofer.ids.framework.messaging.handler.message.MessageHandler;
import de.fraunhofer.ids.framework.messaging.handler.request.RequestMessageHandlerService;
import de.fraunhofer.ids.framework.messaging.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.net.URL;

import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@ContextConfiguration(classes = {RequestMessageHandlerService.class, MessageDispatcherProvider.class, MessageDispatcherTest.TestContextConfiguration.class})
class MessageDispatcherTest {

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

    @Autowired
    private MessageDispatcherProvider messageDispatcherProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private de.fraunhofer.ids.framework.messaging.handler.request.RequestMessageHandler requestMessageHandler;

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

    @Test
    public void testMessageDispatcher() throws Exception{
        Mockito.when(configurationContainer.getConnector()).thenReturn(connector);
        Mockito.when(connector.getId()).thenReturn(new URL("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5").toURI());
        Mockito.when(connector.getOutboundModelVersion()).thenReturn("1.0.3");
        Mockito.when(configurationContainer.getConfigModel()).thenReturn(configurationModel);
        Mockito.when(configurationModel.getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
        Mockito.when(provider.provideDapsToken()).thenReturn("Mocked Token.");
        Mockito.when(publicKeyProvider.providePublicKey()).thenReturn(null);
        Mockito.when(dapsValidator.checkDat(Mockito.anyString())).thenReturn(true);
        Mockito.when(dapsValidator.checkDat(Mockito.any(Message.class))).thenReturn(true);

        var dispatcher = messageDispatcherProvider.provideMessageDispatcher(objectMapper, requestMessageHandler, publicKeyProvider, configurationContainer);
        var reqMsg = new RequestMessageBuilder().build();
        var notMsg = new NotificationMessageBuilder().build();
        ErrorResponse requestResponse = (ErrorResponse) dispatcher.process(reqMsg, null);
        assertTrue(requestResponse.getErrorMessage().equals("request")); //use error message to check which handler got the message
        ErrorResponse notificationResponse = (ErrorResponse) dispatcher.process(notMsg, null);
        assertTrue(notificationResponse.getErrorMessage().equals("notification")); //use error message to check which handler got the message
    }

}