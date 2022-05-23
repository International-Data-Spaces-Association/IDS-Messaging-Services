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
package ids.messaging.clearinghouse;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.LogMessageBuilder;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.ResultMessageBuilder;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.util.Util;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import ids.messaging.core.daps.DapsPublicKeyProvider;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.core.daps.DapsValidator;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.http.IdsHttpService;
import ids.messaging.protocol.multipart.MultipartResponseConverter;
import ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import ids.messaging.protocol.multipart.mapping.ResultMAP;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import ids.messaging.requests.MessageTemplate;
import ids.messaging.requests.NotificationTemplateProvider;
import ids.messaging.requests.RequestTemplateProvider;
import ids.messaging.requests.builder.IdsRequestBuilderService;
import ids.messaging.util.IdsMessageUtils;
import okhttp3.MultipartBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = {ClearingHouseService.class})
class ClearingHouseServiceTest {

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

    @MockBean
    private IdsRequestBuilderService idsRequestBuilderService;

    @MockBean
    private MultipartResponseConverter multipartResponseConverter;

    @MockBean
    private NotificationTemplateProvider notificationTemplateProvider;

    @MockBean
    private RequestTemplateProvider requestTemplateProvider;

    @Autowired
    private IDSClearingHouseService idsClearingHouseService;

    private DynamicAttributeToken fakeToken;

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
        MessageTemplate logMsg = () -> new LogMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(configurationContainer.getConnector().getOutboundModelVersion())
                ._issuerConnector_(configurationContainer.getConnector().getId())
                ._senderAgent_(configurationContainer.getConnector().getId())
                ._securityToken_(dapsTokenProvider.getDAT())
                .build();
        MessageTemplate queryMsg = () -> new QueryMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._modelVersion_(configurationContainer.getConnector().getOutboundModelVersion())
                ._issuerConnector_(configurationContainer.getConnector().getId())
                ._senderAgent_(configurationContainer.getConnector().getId())
                ._securityToken_(dapsTokenProvider.getDAT())
                ._queryLanguage_(QueryLanguage.SPARQL)
                ._queryScope_(QueryScope.ALL)
                ._recipientScope_(QueryTarget.CLEARING_HOUSE)
                .build();
        Mockito.when(notificationTemplateProvider.logMessageTemplate(Mockito.any())).thenReturn(logMsg);
        Mockito.when(requestTemplateProvider.queryMessageTemplate(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(queryMsg);
    }

    @Test
    void testSendLogToClearingHouse() throws Exception {
        final var message = new MessageProcessedNotificationMessageBuilder()
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
        final Map<String, String> map = new ConcurrentHashMap<>();
        map.put(MultipartDatapart.HEADER.toString(), message.toRdf());
        map.put(MultipartDatapart.PAYLOAD.toString(), "");
        Mockito.when(idsHttpService.sendAndCheckDat(any(MultipartBody.class),any(URI.class)))
               .thenReturn(map);
        Mockito.when(multipartResponseConverter.convertResponse(any(Map.class)))
               .thenReturn(new MessageProcessedNotificationMAP(message));
        final var result = idsClearingHouseService.sendLogToClearingHouse(message, "id");
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.getClass(), "Method should return MessageProcessedNotificationMessage");


    }

    @Test
    void testQueryClearingHouse() throws Exception {
        final var message = new ResultMessageBuilder()
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
        final Map<String, String> map = new ConcurrentHashMap<>();
        map.put(MultipartDatapart.HEADER.toString(), message.toRdf());
        map.put(MultipartDatapart.PAYLOAD.toString(), "");
        Mockito.when(idsHttpService.sendAndCheckDat(any(MultipartBody.class),any(URI.class)))
               .thenReturn(map);
        Mockito.when(multipartResponseConverter.convertResponse(any(Map.class)))
               .thenReturn(new ResultMAP(message, "result"));
        final var result = idsClearingHouseService.queryClearingHouse("","", QueryLanguage.SPARQL,QueryScope.ALL,QueryTarget.BROKER, null);
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(ResultMAP.class, result.getClass(), "Method should return MessageProcessedNotificationMessage");
    }
}
