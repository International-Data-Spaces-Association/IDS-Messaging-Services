package de.fraunhofer.ids.messaging.protocol;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.ConfigurationModelBuilder;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.ConnectorStatus;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.LogLevel;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.config.ClientProvider;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsValidator;
import de.fraunhofer.ids.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { InfrastructureServiceTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
class InfrastructureServiceTest {

    private final MockWebServer mockWebServer = new MockWebServer();

    @Autowired
    private InfrastructureService infrastructureService;

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

        @Bean
        InfrastructureService getInfrastructureService(){
            return new InfrastructureService(container, tokenProvider, getMessageService());
        }

    }

    @Test
    void testInfrastructureService() throws
            IOException,
            DapsTokenManagerException,
            ClaimsException,
            MultipartParseException,
            UnknownResponseException,
            DeserializeException,
            UnexpectedResponseException,
            SerializeException,
            ShaclValidatorException,
            SendMessageException {
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

        //setup mockwebserver response
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
        final var multipartString = buffer.readUtf8();
        mockWebServer.enqueue(new MockResponse().setBody(multipartString));

        //request selfdescription and check if MAP handles response correctly
        final var map = infrastructureService.requestSelfDescription(mockWebServer.url("/").uri());
        assertEquals(descRespMsg, map.getMessage());
        assertEquals(connector, serializer.deserialize(map.getPayload().get(), Connector.class));
    }

}
