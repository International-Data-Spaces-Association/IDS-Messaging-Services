package de.fraunhofer.ids.framework.broker;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.framework.daps.DapsPublicKeyProvider;
import de.fraunhofer.ids.framework.daps.DapsTokenProvider;
import de.fraunhofer.ids.framework.daps.DapsValidator;
import de.fraunhofer.ids.framework.messaging.protocol.MessageService;
import de.fraunhofer.ids.framework.messaging.protocol.http.IdsHttpService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ContextConfiguration(classes = { BrokerServiceTest.TestContextConfiguration.class})
class BrokerServiceTest {

    //NotificationMessage to be returned by Mockwebserver
    static final String MESSAGE_BODY = "--msgpart\r\n"
            + "Content-Disposition: form-data; name=\"header\"\r\n"
            + "Content-Length: 2355\r\n"
            + "\r\n"
            + "{\r\n"
            + "  \"@context\" : {\r\n"
            + "    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n"
            + "    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n"
            + "  },\r\n"
            + "  \"@type\" : \"ids:MessageProcessedNotificationMessage\",\r\n"
            + "  \"@id\" : \"https://w3id.org/idsa/autogen/connectorUpdateMessage/0a019211-16bb-4c5a-aa14-8708994d1be4\",\r\n"
            + "  \"ids:senderAgent\" : {\r\n"
            + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\r\n"
            + "  },\r\n"
            + "  \"ids:issued\" : {\r\n"
            + "    \"@value\" : \"2021-02-25T08:48:18.958+01:00\",\r\n"
            + "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n"
            + "  },\r\n"
            + "  \"ids:issuerConnector\" : {\r\n"
            + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\r\n"
            + "  },\r\n"
            + "  \"ids:modelVersion\" : \"4.0.0\",\r\n"
            + "  \"ids:securityToken\" : {\r\n"
            + "    \"@type\" : \"ids:DynamicAttributeToken\",\r\n"
            + "    \"@id\" : \"https://w3id.org/idsa/autogen/dynamicAttributeToken/3bbc1b81-f9ac-4469-ac88-18e5c07b50f5\",\r\n"
            + "    \"ids:tokenFormat\" : {\r\n"
            + "      \"@id\" : \"idsc:JWT\"\r\n"
            + "    },\r\n"
            + "    \"ids:tokenValue\" : \"eyJ0eXAiOiJKV1QiLCJraWQiOiJkZWZhdWx0IiwiYWxnIjoiUlMyNTYifQ.eyJzZWN1cml0eVByb2ZpbGUiOiJpZHNjOkJBU0VfQ09OTkVDVE9SX1NFQ1VSSVRZX1BST0ZJTEUiLCJyZWZlcnJpbmdDb25uZWN0b3IiOiJodHRwOi8vaXNzdF9pZHNfZnJhbWV3b3JrX2RlbW9fY29ubmVjdG9yLmRlbW8iLCJAdHlwZSI6ImlkczpEYXRQYXlsb2FkIiwiQGNvbnRleHQiOiJodHRwczovL3czaWQub3JnL2lkc2EvY29udGV4dHMvY29udGV4dC5qc29ubGQiLCJ0cmFuc3BvcnRDZXJ0c1NoYTI1NiI6IjNlNDA3N2Q2MTI2Y2MxNmMyOGY0NzEyOTU1N2I1YmMzOGFmNzlhMmVmN2UzN2FkMDZiMDBmOTc4Y2FjNzNiNzkiLCJzY29wZXMiOlsiaWRzYzpJRFNfQ09OTkVDVE9SX0FUVFJJQlVURVNfQUxMIl0sImF1ZCI6Imlkc2M6SURTX0NPTk5FQ1RPUlNfQUxMIiwiaXNzIjoiaHR0cHM6Ly9kYXBzLmFpc2VjLmZyYXVuaG9mZXIuZGUiLCJzdWIiOiIzODpBMTpGNDpEMTozNzoxNjpDRTo3RjoxNjozNDo4Mzo3MjpFMjpDQjo3RjpDQzozNjo4NjpDMTo2QTprZXlpZDpDQjo4QzpDNzpCNjo4NTo3OTpBODoyMzpBNjpDQjoxNTpBQjoxNzo1MDoyRjpFNjo2NTo0Mzo1RDpFOCIsIm5iZiI6MTYxNDIzOTI5OCwiaWF0IjoxNjE0MjM5Mjk4LCJqdGkiOiJNVGMyT1RnNE1USTFPVEE0TlRFME9Ea3hNQT09IiwiZXhwIjoxNjE0MjQyODk4fQ.YtnL9H3qzw1iTpCmGdIXY4AkkvT-UE1dEoHheyl5vYRaM30r1R8i_61jcL9ZyzoHO3TfXx5PASa_j44ePrtpgpqm700-i_BtkGYbcBqHG81TcmyDyMnoM54v5MknHt5tK28ZNp1A_mbavD0t1GoM_iV3DjwBjSXNIZa9igw_5ryzaqjWLSs6ZgV6oXU2J41zKviWPTT6tnGY1oKTt56x4n_DxNDPwYAZ5x23UFDOEoLbnMDnh-aqDGpwpaEPBUkd-a1B7YfDSYWLvjLJCco3rYlqBpmXT37oyKlkgU9TKSqve-dDQ8XNQZSUN_qKXOOOZWuX2thkzAo3sXXztX4s8w\"\r\n"
            + "  },\r\n"
            + "  \"ids:affectedConnector\" : {\r\n"
            + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\r\n"
            + "  },\r\n"
            + "  \"ids:correlationMessage\" : {\r\n"
            + "    \"@id\" : \"https://w3id.org/idsa/autogen/message/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\r\n"
            + "  }\r\n"
            + "}\r\n"
            + "--msgpart\r\n"
            + "Content-Disposition: form-data; name=\"payload\"\r\n"
            + "Content-Length: 533\r\n"
            + "\r\n"
            + "{\r\n"
            + "  \"@context\" : {\r\n"
            + "    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n"
            + "    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n"
            + "  },\r\n"
            + "  \"@type\" : \"ids:BaseConnector\",\r\n"
            + "  \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\",\r\n"
            + "  \"ids:curator\" : {\r\n"
            + "    \"@id\" : \"https://example.com\"\r\n"
            + "  },\r\n"
            + "  \"ids:maintainer\" : {\r\n"
            + "    \"@id\" : \"https://example.com\"\r\n"
            + "  },\r\n"
            + "  \"ids:securityProfile\" : {\r\n"
            + "    \"@id\" : \"idsc:BASE_SECURITY_PROFILE\"\r\n"
            + "  },\r\n"
            + "  \"ids:outboundModelVersion\" : \"4.0.0\",\r\n"
            + "  \"ids:inboundModelVersion\" : [ \"4.0.0\" ]\r\n"
            + "}\r\n"
            + "--msgpart--\r\n";
    @Autowired
    Connector connector;

    @Autowired
    KeyStoreManager keyStoreManager;

    @Autowired
    ConfigurationModel configurationModel;

    @Autowired
    ConfigContainer   configurationContainer;

    @Autowired
    DapsTokenProvider dapsTokenProvider;

    @Autowired
    DapsPublicKeyProvider dapsPublicKeyProvider;

    @Autowired
    DapsValidator dapsValidator;

    @Autowired
    MessageService messageService;

    IdsHttpService    idsHttpService;

    MockWebServer     mockWebServer;

    @Autowired
    BrokerService     brokerService;

    @Autowired
    ClientProvider clientProvider;

    @Configuration
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class TestContextConfiguration{
        @MockBean
        KeyStoreManager keyStoreManager;

        @MockBean
        ConfigurationModel configurationModel;

        @MockBean
        ConfigContainer   configurationContainer;

        @MockBean
        DapsTokenProvider dapsTokenProvider;

        @MockBean
        DapsPublicKeyProvider dapsPublicKeyProvider;

        @MockBean
        DapsValidator dapsValidator;

        @MockBean
        ClientProvider clientProvider;

        @Bean
        public Serializer getSerializer(){
            return new Serializer();
        }

        @Bean
        public BrokerService getBrokerService() {
            return new BrokerService(configurationContainer, dapsTokenProvider, getMessageService());
        }

        @Bean
        IdsHttpService getHttpService(){
            return new IdsHttpService(clientProvider, dapsValidator, configurationContainer, new Serializer());
        }

        @Bean
        public MessageService getMessageService(){
            return new MessageService(getHttpService());
        }

        @Bean
        Connector getConnector() {
            return new BaseConnectorBuilder()
                    ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                    ._inboundModelVersion_(new ArrayList<>(List.of("1.2.3")))
                    ._outboundModelVersion_("1.0.3")
                    ._maintainer_(URI.create("https://maintainer"))
                    ._curator_(URI.create("https://curator"))
                    .build();
        }
    }

    @Test
    void testUpdateSelfDescriptionAtBrokers ()throws Exception {
        //Configure Mockito
        final var fakeToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_("fake Token")
                .build();
        Mockito.when(configurationContainer.getConnector()).thenReturn(connector);
        Mockito.when(configurationContainer.getConfigurationModel()).thenReturn(configurationModel);
        Mockito.when(clientProvider.getClient()).thenReturn(new OkHttpClient());
        Mockito.when(configurationModel.getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
        Mockito.when(dapsTokenProvider.provideDapsToken()).thenReturn("Mocked Token.");
        Mockito.when(dapsTokenProvider.getDAT()).thenReturn(fakeToken);
        Mockito.when(dapsPublicKeyProvider.providePublicKeys()).thenReturn(null);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        //Create MockWebServer returning MESSAGE_BODY NotificationMessage
        this.mockWebServer = new MockWebServer();
        final var mockResponse = new MockResponse().setBody(MESSAGE_BODY);
        this.mockWebServer.enqueue(mockResponse);
        //send message and check the response
        final var map = this.brokerService.updateSelfDescriptionAtBroker(URI.create(mockWebServer.url("/").toString()));
        System.out.println(new Serializer().serialize(map.getMessage()));
        assertNotNull(map.getMessage());
    }

}
