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
package ids.messaging.appstore;

import java.net.URI;

import de.fraunhofer.iais.eis.AppResourceBuilder;
import de.fraunhofer.iais.eis.AppStore;
import de.fraunhofer.iais.eis.AppStoreBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
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
import ids.messaging.protocol.multipart.mapping.ArtifactResponseMAP;
import ids.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import ids.messaging.requests.NotificationTemplateProvider;
import ids.messaging.requests.RequestTemplateProvider;
import ids.messaging.requests.builder.IdsRequestBuilderService;
import ids.messaging.util.IdsMessageUtils;
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

@ExtendWith( SpringExtension.class)
@ContextConfiguration(classes = { AppStoreServiceTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
class AppStoreServiceTest  {
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

    @Autowired
    private AppStoreService appStoreService;

    private DynamicAttributeToken fakeToken;

    private MessageProcessedNotificationMessage notificationMessage;

    private ResultMessage resultMessage;
    private AppStore appStore;


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

        @Bean
        RequestTemplateProvider getRequestTemplateProvider(){
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
        public AppStoreService getBrokerService() {
            return new AppStoreService(configurationContainer, dapsTokenProvider, messageService, getIdsRequestBuilderService());
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
        appStore = new AppStoreBuilder()
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._outboundModelVersion_("4.0.0")
                ._inboundModelVersion_(Util.asList("4.0.0"))
                ._curator_(URI.create("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5"))
                ._maintainer_(URI.create("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5"))
                ._hasDefaultEndpoint_(endpoint)
                .build();
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
     void testRequestAppStoreDescription() throws Exception{
        final var message = new DescriptionResponseMessageBuilder()
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
        final MessageAndPayload map = new DescriptionResponseMAP(message, appStore.toRdf());
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.appStoreService.requestAppStoreDescription(URI.create("/"));
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(DescriptionResponseMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return DescriptionResponseMessage");
        assertNotNull(result.getReceivedPayload(), "DescriptionResponseMessage should have a payload");
    }

    @Test
    void testRequestAppDescription() throws Exception {
        final var message = new DescriptionResponseMessageBuilder()
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
        final MessageAndPayload map = new DescriptionResponseMAP(message, new AppResourceBuilder().build().toRdf());
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.appStoreService.requestAppDescription(URI.create("/"), URI.create("/"));
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(DescriptionResponseMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return DescriptionResponseMessage");
        assertNotNull(result.getReceivedPayload(), "DescriptionResponseMessage should have a payload");
    }

    @Test
    void testRequestAppArtifact() throws Exception{
        final var message = new ArtifactResponseMessageBuilder()
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
        final MessageAndPayload map = new ArtifactResponseMAP(message, "");
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);

        final var result = this.appStoreService.requestAppArtifact(URI.create("/"), URI.create("/"));
        assertNotNull(result.getUnderlyingMessage(), "Method should return a message");
        assertTrue(ArtifactResponseMessage.class.isAssignableFrom(result.getUnderlyingMessage().getClass()), "Method should return ArtifactResponseMessage");
        assertNotNull(result.getReceivedPayload(), "ArtifactResponseMessage should have a payload");
    }
}
