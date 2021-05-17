package de.fraunhofer.ids.framework.broker;

import java.net.URI;
import java.util.ArrayList;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.messaging.broker.BrokerService;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.messaging.core.daps.DapsPublicKeyProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsValidator;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ResultMAP;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(SpringExtension.class)
//@SpringBootTest( webEnvironment= SpringBootTest.WebEnvironment.NONE )
//@EnableConfigurationProperties(value = ConfigProperties.class)
@ContextConfiguration(classes = { BrokerServiceTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
public class BrokerServiceTest {

    @Autowired
    private  Connector connector;

    @Autowired
    private ConfigurationModel configurationModel;

    @Autowired
    private ConfigContainer configurationContainer;

    @Autowired
    private DapsTokenProvider dapsTokenProvider;

    @Autowired
    private DapsPublicKeyProvider dapsPublicKeyProvider;

    @Autowired
    private DapsValidator dapsValidator;

    @Autowired
    private MessageService messageService;

    private MockWebServer mockWebServer;

    @Autowired
    private BrokerService brokerService;

    private DynamicAttributeToken fakeToken;

    private MessageProcessedNotificationMessage message;


    @Configuration
    static class TestContextConfiguration {

        @Bean
        public Serializer getSerializer() {
            return new Serializer();
        }

        @Bean
        public BrokerService getBrokerService() {
            return new BrokerService(configurationContainer, dapsTokenProvider, messageService);
        }

        @MockBean
        private KeyStoreManager keyStoreManager;

        @MockBean
        private ConfigurationModel configurationModel;

        @MockBean
        private ConfigContainer configurationContainer;

        @MockBean
        private DapsTokenProvider dapsTokenProvider;

        @MockBean
        private DapsPublicKeyProvider dapsPublicKeyProvider;

        @MockBean
        private DapsValidator dapsValidator;

        @MockBean
        private IdsHttpService idsHttpService;

        @MockBean
        private MessageService messageService;

        @MockBean
        private Connector connector;
    }


    @BeforeEach
    public void setUp() throws Exception{
        this.fakeToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_("fake Token")
                .build();
        final var endpoint = new ConnectorEndpointBuilder()
                ._accessURL_(URI.create("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5"))
                .build();
        connector = new BaseConnectorBuilder()
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._outboundModelVersion_("4.0.0")
                ._inboundModelVersion_(Util.asList("4.0.0"))
                ._curator_(URI.create("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5"))
                ._maintainer_(URI.create("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5"))
                ._hasDefaultEndpoint_(endpoint)
                .build();
        Mockito.when(configurationContainer.getConnector()).thenReturn(connector);
        Mockito.when(configurationContainer.getConfigurationModel()).thenReturn(configurationModel);
        Mockito.when(configurationModel.getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
        Mockito.when(dapsTokenProvider.provideDapsToken()).thenReturn("Mocked Token.");
        Mockito.when(dapsPublicKeyProvider.providePublicKeys()).thenReturn(null);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsTokenProvider.getDAT()).thenReturn(fakeToken);
        this. message = new MessageProcessedNotificationMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                ._senderAgent_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                ._securityToken_(this.fakeToken)
                ._modelVersion_("4.0.0")
                ._correlationMessage_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                .build();
    }

    @Test
    public void testUpdateSelfDescriptionAtBroker() throws Exception {

        final MessageAndPayload map = new MessageProcessedNotificationMAP(message);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.updateSelfDescriptionAtBroker(URI.create("/"));
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.getClass(), "Method should return MessageProcessedNotificationMessage");
    }


    @Test
    public void testRemoveResourceFromBroker() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(message);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.removeResourceFromBroker(URI.create("/"), new ResourceBuilder().build());
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.getClass(), "Method should return MessageProcessedNotificationMessage");
    }

    @Test
    public void testUpdateResourceAtBroker() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(message);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var  result = this.brokerService.updateResourceAtBroker(URI.create("/"), new ResourceBuilder().build());
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.getClass(), "Method should return MessageProcessedNotificationMessage");
    }

    @Test
    public void testUnregisterAtBroker() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(message);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.unregisterAtBroker(URI.create("/"));
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.getClass(), "Method should return MessageProcessedNotificationMessage");
    }

    @Test
    public void testUpdateSelfDescriptionAtBrokers() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(message);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);
        final var list = new ArrayList<URI>();
        list.add(URI.create("/"));
        list.add(URI.create("/"));


        final var result = this.brokerService.updateSelfDescriptionAtBrokers(list);

        assertNotNull(result.get(0).getMessage(),"Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.get(0).getClass(), "Method should return MessageProcessedNotificationMessage");
        assertFalse(result.get(0).getPayload().isPresent(),"Payload should be empty for MessageProcessedNotificationMAPs");
        assertNotNull(result.get(1).getMessage(), "Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.get(1).getClass(), "Method should return MessageProcessedNotificationMessage");
        assertFalse(result.get(1).getPayload().isPresent(),"Payload should be empty for MessageProcessedNotificationMAPs");
    }

    @Test
    public void testEmptyList() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(message);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.updateSelfDescriptionAtBrokers(
                new ArrayList<>());


        assertEquals(new ArrayList<MessageProcessedNotificationMAP>(), result, "Should return empty list when no URIs are passed");
    }

    @Test
    public void testQueryBroker() throws Exception{
        final var resultMessage = new ResultMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                ._senderAgent_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                ._securityToken_(this.fakeToken)
                ._modelVersion_("4.0.0")
                ._correlationMessage_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                .build();
        final MessageAndPayload map = new ResultMAP(resultMessage, "This is the QueryResult");
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.queryBroker(URI.create("/"), "",QueryLanguage.SPARQL,QueryScope.ALL,QueryTarget.BROKER);
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(ResultMAP.class, result.getClass(), "Method should return ResultMap");
        assertTrue(result.getPayload().isPresent(),"ResultMAP should have payload");
        assertNotNull(result.getPayload().get(),"ResultMAP should have payload");
    }

}