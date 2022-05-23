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
package ids.messaging.requests;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.ConfigurationModelBuilder;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.ConnectorStatus;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.LogLevel;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.core.config.ClientProvider;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsTokenManagerException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.core.daps.DapsValidator;
import ids.messaging.protocol.MessageService;
import ids.messaging.protocol.UnexpectedResponseException;
import ids.messaging.protocol.http.IdsHttpService;
import ids.messaging.protocol.multipart.mapping.ArtifactResponseMAP;
import ids.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import ids.messaging.util.IdsMessageUtils;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { InfrastructureServiceTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
class InfrastructureServiceTest {

    private final MockWebServer mockWebServer = new MockWebServer();

    @Autowired
    private Serializer serializer;

    @Autowired
    private ConfigContainer configContainer;

    @Autowired
    private DapsTokenProvider provider;

    @Autowired
    private ClientProvider clientProvider;

    @Configuration
    static class TestContextConfiguration {
        @MockBean
        ConfigContainer container;

        @MockBean
        DapsTokenProvider tokenProvider;

        @MockBean
        ClientProvider clientProvider;

        @MockBean
        DapsValidator dapsValidator;

        @Bean
        Serializer getSerializer(){
            return new Serializer();
        }

        @Bean
        IdsHttpService getHttpService(){
            return new IdsHttpService(clientProvider, dapsValidator, container, getSerializer());
        }

        @Bean
        MessageService getMessageService(){
            return new MessageService(getHttpService());
        }

    }

    @Test
    void testInfrastructureService()
            throws IOException,
            DapsTokenManagerException {
        //setup mockito
        final var connector = new BaseConnectorBuilder()
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._inboundModelVersion_(List.of("1.0.0"))
                ._outboundModelVersion_("1.0.0")
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("http://example.com")).build())
                ._maintainer_(URI.create("http://example.com"))
                ._curator_(URI.create("http://example.com"))
                .build();

        final var configurationModel = new ConfigurationModelBuilder()
                ._configurationModelLogLevel_(LogLevel.DEBUG_LEVEL_LOGGING)
                ._connectorDeployMode_(ConnectorDeployMode.TEST_DEPLOYMENT)
                ._connectorStatus_(ConnectorStatus.CONNECTOR_OFFLINE)
                .build();

        Mockito.when(configContainer.getConnector()).thenReturn(connector);
        Mockito.when(configContainer.getConfigurationModel()).thenReturn(configurationModel);
        Mockito.when(clientProvider.getClient()).thenReturn(new OkHttpClient());
        Mockito.when(provider.getDAT()).thenReturn(
                new DynamicAttributeTokenBuilder()
                        ._tokenValue_("string")
                        ._tokenFormat_(TokenFormat.JWT)
                        .build()
        );

        final var infrastructureService = Mockito.mock(
                InfrastructureService.class,
                Mockito.CALLS_REAL_METHODS
        );

        final var descRespMsg = new DescriptionResponseMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
        final var multipart = new MultipartBody.Builder()
                .addFormDataPart("header", serializer.serialize(descRespMsg))
                .addFormDataPart("payload", serializer.serialize(connector))
                .build();
        final Buffer buffer = new Buffer();
        multipart.writeTo(buffer);
        DescriptionResponseMAP map = new DescriptionResponseMAP(descRespMsg, "payload");
        assertDoesNotThrow(() -> infrastructureService.expectMapOfTypeT(map, DescriptionResponseMAP.class));
        assertThrows(UnexpectedResponseException.class, () -> infrastructureService.expectMapOfTypeT(map, ArtifactResponseMAP.class));
    }

}
