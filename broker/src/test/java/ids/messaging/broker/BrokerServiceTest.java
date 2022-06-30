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
package ids.messaging.broker;

import java.net.URI;

import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.iais.eis.ResultMessageBuilder;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import ids.messaging.core.daps.DapsPublicKeyProvider;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.core.daps.DapsValidator;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.http.IdsHttpService;
import ids.messaging.protocol.multipart.MessageAndPayload;
import ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import ids.messaging.protocol.multipart.mapping.ResultMAP;
import ids.messaging.requests.NotificationTemplateProvider;
import ids.messaging.requests.RequestTemplateProvider;
import ids.messaging.requests.builder.IdsRequestBuilderService;
import ids.messaging.util.IdsMessageUtils;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { BrokerServiceTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
class BrokerServiceTest {

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

    private MessageProcessedNotificationMessage notificationMessage;

    private ResultMessage resultMessage;


    @Configuration
    static class TestContextConfiguration {
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

        @Bean
        public NotificationTemplateProvider getNotificationTemplateProvider(){
            return new NotificationTemplateProvider(configurationContainer, dapsTokenProvider);
        }

        @Bean RequestTemplateProvider getRequestTemplateProvider(){
            return new RequestTemplateProvider(configurationContainer, dapsTokenProvider);
        }

        @Bean
        public IdsRequestBuilderService getIdsRequestBuilderService(){
            return new IdsRequestBuilderService(messageService, getRequestTemplateProvider(), getNotificationTemplateProvider());
        }

        @Bean
        public Serializer getSerializer() {
            return new Serializer();
        }

        @Bean
        public BrokerService getBrokerService() {
            return new BrokerService(configurationContainer, dapsTokenProvider, messageService, getIdsRequestBuilderService(), getNotificationTemplateProvider(), getRequestTemplateProvider());
        }
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
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsTokenProvider.getDAT()).thenReturn(fakeToken);
        this.notificationMessage = new MessageProcessedNotificationMessageBuilder()
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
        this.resultMessage = new ResultMessageBuilder()
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
    void testUpdateSelfDescriptionAtBroker() throws Exception {

        final MessageAndPayload map = new MessageProcessedNotificationMAP(notificationMessage);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.updateSelfDescriptionAtBroker(URI.create("/"));
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(MessageProcessedNotificationMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return MessageProcessedNotificationMessage");
    }


    @Test
    void testRemoveResourceFromBroker() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(notificationMessage);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.removeResourceFromBroker(URI.create("/"), new ResourceBuilder().build());
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(MessageProcessedNotificationMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return MessageProcessedNotificationMessage");
    }

    @Test
    void testUpdateResourceAtBroker() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(notificationMessage);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var  result = this.brokerService.updateResourceAtBroker(URI.create("/"), new ResourceBuilder().build());
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(MessageProcessedNotificationMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return MessageProcessedNotificationMessage");
    }

    @Test
    void testUnregisterAtBroker() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(notificationMessage);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.unregisterAtBroker(URI.create("/"));
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(MessageProcessedNotificationMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return MessageProcessedNotificationMessage");
    }

    @Test
    void testQueryBroker() throws Exception{

        final MessageAndPayload map = new ResultMAP(resultMessage, "This is the QueryResult");
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.queryBroker(URI.create("/"), "",QueryLanguage.SPARQL,QueryScope.ALL,QueryTarget.BROKER);
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(ResultMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return ResultMessage");
        assertNotNull(result.getReceivedPayload(), "ResultMessage should have a payload");
    }

    @Test
    void testFtSearchBroker() throws Exception{
        final MessageAndPayload map = new ResultMAP(resultMessage, "This is the QueryResult");
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.brokerService.fullTextSearchBroker(URI.create("/"), "", QueryScope.ALL, QueryTarget.BROKER);
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(ResultMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return ResultMessage");
        assertNotNull(result.getReceivedPayload(), "ResultMessage should have a payload");
    }

}
