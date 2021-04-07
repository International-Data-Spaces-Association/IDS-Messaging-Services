package de.fraunhofer.ids.framework.broker;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.config.ConfigProducer;
import de.fraunhofer.ids.framework.config.ConfigProperties;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManagerInitializationException;
import de.fraunhofer.ids.framework.daps.*;
import de.fraunhofer.ids.framework.daps.aisec.AisecTokenManagerService;
import de.fraunhofer.ids.framework.messaging.dispatcher.filter.PreDispatchingFilterResult;
import de.fraunhofer.ids.framework.messaging.protocol.MessageService;
import de.fraunhofer.ids.framework.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.framework.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import junit.framework.TestCase;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
//@SpringBootTest( webEnvironment= SpringBootTest.WebEnvironment.NONE )
//@EnableConfigurationProperties(value = ConfigProperties.class)
@ContextConfiguration(classes = { BrokerServiceTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
public class BrokerServiceTest {

    @Autowired
    private Connector connector;

    @Autowired
    private KeyStoreManager keyStoreManager;

    @Autowired
    private ConfigurationModel configurationModel;

    @Autowired
    private ConfigContainer   configurationContainer;

    @Autowired
    private DapsTokenProvider dapsTokenProvider;

    @Autowired
    private DapsPublicKeyProvider dapsPublicKeyProvider;

    @Autowired
    private DapsValidator dapsValidator;

    @Autowired
    private MessageService messageService;

    private IdsHttpService    idsHttpService;

    private MockWebServer     mockWebServer;

    @Autowired
    private BrokerService     brokerService;


    @Configuration
    static class TestContextConfiguration{

        @Bean
        public Serializer getSerializer(){
            return new Serializer();
        }

        @Bean
        public BrokerService getBrokerService() {
            return new BrokerService(configurationContainer, dapsTokenProvider, getMessageService());
        }
        @MockBean
        private KeyStoreManager keyStoreManager;

        @MockBean
        private ConfigurationModel configurationModel;

        @MockBean
        private ConfigContainer   configurationContainer;

        @MockBean
        private DapsTokenProvider dapsTokenProvider;

        @MockBean
        private DapsPublicKeyProvider dapsPublicKeyProvider;

        @MockBean
        private DapsValidator dapsValidator;

        @MockBean
        private IdsHttpService  idsHttpService;

        @Bean
        public MessageService getMessageService(){
            return new MessageService(idsHttpService);
        }


        @MockBean
        private Connector connector;
    }

    @Test
    public void testUpdateSelfDescriptionAtBrokers ()throws Exception {
        DynamicAttributeToken fakeToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_("fake Token")
                .build();
        Mockito.when(configurationContainer.getConnector()).thenReturn(connector);
        Mockito.when(connector.getId()).thenReturn(new URL("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5").toURI());
        Mockito.when(connector.getOutboundModelVersion()).thenReturn("1.0.3");
        Mockito.when(configurationContainer.getConfigurationModel()).thenReturn(configurationModel);
        Mockito.when(configurationModel.getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
        Mockito.when(dapsTokenProvider.provideDapsToken()).thenReturn("Mocked Token.");
        Mockito.when(dapsPublicKeyProvider.providePublicKeys()).thenReturn(null);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsTokenProvider.getDAT()).thenReturn(fakeToken);
        //this.brokerService = new BrokerService(configurationContainer, clientProvider, dapsTokenProvider, idsHttpService);
        this.mockWebServer = new MockWebServer();
        MockResponse mockResponse = new MockResponse().setBody("--msgpart\n"
                                                               + "Content-Disposition: form-data; name=\"header\"\n"
                                                               + "Content-Length: 2355\n"
                                                               + "\n"
                                                               + "{\n"
                                                               + "  \"@context\" : {\n"
                                                               + "    \"ids\" : \"https://w3id.org/idsa/core/\",\n"
                                                               + "    \"idsc\" : \"https://w3id.org/idsa/code/\"\n"
                                                               + "  },\n"
                                                               + "  \"@type\" : \"ids:ConnectorUpdateMessage\",\n"
                                                               + "  \"@id\" : \"https://w3id.org/idsa/autogen/connectorUpdateMessage/0a019211-16bb-4c5a-aa14-8708994d1be4\",\n"
                                                               + "  \"ids:senderAgent\" : {\n"
                                                               + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:issued\" : {\n"
                                                               + "    \"@value\" : \"2021-02-25T08:48:18.958+01:00\",\n"
                                                               + "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:issuerConnector\" : {\n"
                                                               + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:modelVersion\" : \"4.0.0\",\n"
                                                               + "  \"ids:securityToken\" : {\n"
                                                               + "    \"@type\" : \"ids:DynamicAttributeToken\",\n"
                                                               + "    \"@id\" : \"https://w3id.org/idsa/autogen/dynamicAttributeToken/3bbc1b81-f9ac-4469-ac88-18e5c07b50f5\",\n"
                                                               + "    \"ids:tokenFormat\" : {\n"
                                                               + "      \"@id\" : \"idsc:JWT\"\n"
                                                               + "    },\n"
                                                               + "    \"ids:tokenValue\" : \"eyJ0eXAiOiJKV1QiLCJraWQiOiJkZWZhdWx0IiwiYWxnIjoiUlMyNTYifQ.eyJzZWN1cml0eVByb2ZpbGUiOiJpZHNjOkJBU0VfQ09OTkVDVE9SX1NFQ1VSSVRZX1BST0ZJTEUiLCJyZWZlcnJpbmdDb25uZWN0b3IiOiJodHRwOi8vaXNzdF9pZHNfZnJhbWV3b3JrX2RlbW9fY29ubmVjdG9yLmRlbW8iLCJAdHlwZSI6ImlkczpEYXRQYXlsb2FkIiwiQGNvbnRleHQiOiJodHRwczovL3czaWQub3JnL2lkc2EvY29udGV4dHMvY29udGV4dC5qc29ubGQiLCJ0cmFuc3BvcnRDZXJ0c1NoYTI1NiI6IjNlNDA3N2Q2MTI2Y2MxNmMyOGY0NzEyOTU1N2I1YmMzOGFmNzlhMmVmN2UzN2FkMDZiMDBmOTc4Y2FjNzNiNzkiLCJzY29wZXMiOlsiaWRzYzpJRFNfQ09OTkVDVE9SX0FUVFJJQlVURVNfQUxMIl0sImF1ZCI6Imlkc2M6SURTX0NPTk5FQ1RPUlNfQUxMIiwiaXNzIjoiaHR0cHM6Ly9kYXBzLmFpc2VjLmZyYXVuaG9mZXIuZGUiLCJzdWIiOiIzODpBMTpGNDpEMTozNzoxNjpDRTo3RjoxNjozNDo4Mzo3MjpFMjpDQjo3RjpDQzozNjo4NjpDMTo2QTprZXlpZDpDQjo4QzpDNzpCNjo4NTo3OTpBODoyMzpBNjpDQjoxNTpBQjoxNzo1MDoyRjpFNjo2NTo0Mzo1RDpFOCIsIm5iZiI6MTYxNDIzOTI5OCwiaWF0IjoxNjE0MjM5Mjk4LCJqdGkiOiJNVGMyT1RnNE1USTFPVEE0TlRFME9Ea3hNQT09IiwiZXhwIjoxNjE0MjQyODk4fQ.YtnL9H3qzw1iTpCmGdIXY4AkkvT-UE1dEoHheyl5vYRaM30r1R8i_61jcL9ZyzoHO3TfXx5PASa_j44ePrtpgpqm700-i_BtkGYbcBqHG81TcmyDyMnoM54v5MknHt5tK28ZNp1A_mbavD0t1GoM_iV3DjwBjSXNIZa9igw_5ryzaqjWLSs6ZgV6oXU2J41zKviWPTT6tnGY1oKTt56x4n_DxNDPwYAZ5x23UFDOEoLbnMDnh-aqDGpwpaEPBUkd-a1B7YfDSYWLvjLJCco3rYlqBpmXT37oyKlkgU9TKSqve-dDQ8XNQZSUN_qKXOOOZWuX2thkzAo3sXXztX4s8w\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:affectedConnector\" : {\n"
                                                               + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\n"
                                                               + "  }\n"
                                                               + "}\n"
                                                               + "--msgpart\n"
                                                               + "Content-Disposition: form-data; name=\"payload\"\n"
                                                               + "Content-Length: 533\n"
                                                               + "\n"
                                                               + "{\n"
                                                               + "  \"@context\" : {\n"
                                                               + "    \"ids\" : \"https://w3id.org/idsa/core/\",\n"
                                                               + "    \"idsc\" : \"https://w3id.org/idsa/code/\"\n"
                                                               + "  },\n"
                                                               + "  \"@type\" : \"ids:BaseConnector\",\n"
                                                               + "  \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\",\n"
                                                               + "  \"ids:curator\" : {\n"
                                                               + "    \"@id\" : \"https://example.com\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:maintainer\" : {\n"
                                                               + "    \"@id\" : \"https://example.com\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:securityProfile\" : {\n"
                                                               + "    \"@id\" : \"idsc:BASE_SECURITY_PROFILE\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:outboundModelVersion\" : \"4.0.0\",\n"
                                                               + "  \"ids:inboundModelVersion\" : [ \"4.0.0\" ]\n"
                                                               + "}\n"
                                                               + "--msgpart--\n"
                                                               + "\n"
                                                               + "2021-02-25 08:48:19.055  INFO 19212 --- [      IDSdemo-1] d.f.i.f.m.protocol.http.IdsHttpService   : Request is HTTPS: false\n"
                                                               + "2021-02-25 08:48:19.663  INFO 19212 --- [      IDSdemo-1] d.f.ids.framework.broker.BrokerService   : Received response from http://localhost:8080/infrastructure\n"
                                                               + "2021-02-25 08:48:19.806  INFO 19212 --- [      IDSdemo-1] d.f.ids.framework.broker.BrokerService   : --msgpart\n"
                                                               + "Content-Disposition: form-data; name=\"header\"\n"
                                                               + "Content-Length: 2355\n"
                                                               + "\n"
                                                               + "{\n"
                                                               + "  \"@context\" : {\n"
                                                               + "    \"ids\" : \"https://w3id.org/idsa/core/\",\n"
                                                               + "    \"idsc\" : \"https://w3id.org/idsa/code/\"\n"
                                                               + "  },\n"
                                                               + "  \"@type\" : \"ids:ConnectorUpdateMessage\",\n"
                                                               + "  \"@id\" : \"https://w3id.org/idsa/autogen/connectorUpdateMessage/916c8b4f-53c7-4933-be06-83c1dd546dfd\",\n"
                                                               + "  \"ids:senderAgent\" : {\n"
                                                               + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:issued\" : {\n"
                                                               + "    \"@value\" : \"2021-02-25T08:48:19.762+01:00\",\n"
                                                               + "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:issuerConnector\" : {\n"
                                                               + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:modelVersion\" : \"4.0.0\",\n"
                                                               + "  \"ids:securityToken\" : {\n"
                                                               + "    \"@type\" : \"ids:DynamicAttributeToken\",\n"
                                                               + "    \"@id\" : \"https://w3id.org/idsa/autogen/dynamicAttributeToken/fd155390-c5ea-4ba2-96cb-bcc496ac0b1a\",\n"
                                                               + "    \"ids:tokenFormat\" : {\n"
                                                               + "      \"@id\" : \"idsc:JWT\"\n"
                                                               + "    },\n"
                                                               + "    \"ids:tokenValue\" : \"eyJ0eXAiOiJKV1QiLCJraWQiOiJkZWZhdWx0IiwiYWxnIjoiUlMyNTYifQ.eyJzZWN1cml0eVByb2ZpbGUiOiJpZHNjOkJBU0VfQ09OTkVDVE9SX1NFQ1VSSVRZX1BST0ZJTEUiLCJyZWZlcnJpbmdDb25uZWN0b3IiOiJodHRwOi8vaXNzdF9pZHNfZnJhbWV3b3JrX2RlbW9fY29ubmVjdG9yLmRlbW8iLCJAdHlwZSI6ImlkczpEYXRQYXlsb2FkIiwiQGNvbnRleHQiOiJodHRwczovL3czaWQub3JnL2lkc2EvY29udGV4dHMvY29udGV4dC5qc29ubGQiLCJ0cmFuc3BvcnRDZXJ0c1NoYTI1NiI6IjNlNDA3N2Q2MTI2Y2MxNmMyOGY0NzEyOTU1N2I1YmMzOGFmNzlhMmVmN2UzN2FkMDZiMDBmOTc4Y2FjNzNiNzkiLCJzY29wZXMiOlsiaWRzYzpJRFNfQ09OTkVDVE9SX0FUVFJJQlVURVNfQUxMIl0sImF1ZCI6Imlkc2M6SURTX0NPTk5FQ1RPUlNfQUxMIiwiaXNzIjoiaHR0cHM6Ly9kYXBzLmFpc2VjLmZyYXVuaG9mZXIuZGUiLCJzdWIiOiIzODpBMTpGNDpEMTozNzoxNjpDRTo3RjoxNjozNDo4Mzo3MjpFMjpDQjo3RjpDQzozNjo4NjpDMTo2QTprZXlpZDpDQjo4QzpDNzpCNjo4NTo3OTpBODoyMzpBNjpDQjoxNTpBQjoxNzo1MDoyRjpFNjo2NTo0Mzo1RDpFOCIsIm5iZiI6MTYxNDIzOTI5OCwiaWF0IjoxNjE0MjM5Mjk4LCJqdGkiOiJNVGMyT1RnNE1USTFPVEE0TlRFME9Ea3hNQT09IiwiZXhwIjoxNjE0MjQyODk4fQ.YtnL9H3qzw1iTpCmGdIXY4AkkvT-UE1dEoHheyl5vYRaM30r1R8i_61jcL9ZyzoHO3TfXx5PASa_j44ePrtpgpqm700-i_BtkGYbcBqHG81TcmyDyMnoM54v5MknHt5tK28ZNp1A_mbavD0t1GoM_iV3DjwBjSXNIZa9igw_5ryzaqjWLSs6ZgV6oXU2J41zKviWPTT6tnGY1oKTt56x4n_DxNDPwYAZ5x23UFDOEoLbnMDnh-aqDGpwpaEPBUkd-a1B7YfDSYWLvjLJCco3rYlqBpmXT37oyKlkgU9TKSqve-dDQ8XNQZSUN_qKXOOOZWuX2thkzAo3sXXztX4s8w\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:affectedConnector\" : {\n"
                                                               + "    \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\"\n"
                                                               + "  }\n"
                                                               + "}\n"
                                                               + "--msgpart\n"
                                                               + "Content-Disposition: form-data; name=\"payload\"\n"
                                                               + "Content-Length: 533\n"
                                                               + "\n"
                                                               + "{\n"
                                                               + "  \"@context\" : {\n"
                                                               + "    \"ids\" : \"https://w3id.org/idsa/core/\",\n"
                                                               + "    \"idsc\" : \"https://w3id.org/idsa/code/\"\n"
                                                               + "  },\n"
                                                               + "  \"@type\" : \"ids:BaseConnector\",\n"
                                                               + "  \"@id\" : \"https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9\",\n"
                                                               + "  \"ids:curator\" : {\n"
                                                               + "    \"@id\" : \"https://example.com\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:maintainer\" : {\n"
                                                               + "    \"@id\" : \"https://example.com\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:securityProfile\" : {\n"
                                                               + "    \"@id\" : \"idsc:BASE_SECURITY_PROFILE\"\n"
                                                               + "  },\n"
                                                               + "  \"ids:outboundModelVersion\" : \"4.0.0\",\n"
                                                               + "  \"ids:inboundModelVersion\" : [ \"4.0.0\" ]\n"
                                                               + "}\n"
                                                               + "--msgpart--");
        this.mockWebServer.enqueue(mockResponse);
        MessageProcessedNotificationMAP map = this.brokerService.updateSelfDescriptionAtBroker(URI.create(mockWebServer.url("/").toString()));
        System.out.println(new Serializer().serialize(map.getMessage()));
        assertNotNull(map.getMessage());
    }

    private DapsTokenProvider getDapsTokenProvider( ConfigContainer configContainer, ClientProvider clientProvider,
                                                    TokenManagerService tokenManagerService ) {
        return (DapsTokenProvider) getDapsPublicKeyProvider(configContainer, clientProvider, tokenManagerService);
    }


    @NotNull
    private KeyStoreManager getKeyStoreManager( ConfigurationModel configurationModel, ConfigProperties properties )
            throws KeyStoreManagerInitializationException {
        return new KeyStoreManager(configurationModel, properties.getKeyStorePassword().toCharArray(),
                                   properties.getTrustStorePassword().toCharArray(),
                                   properties.getKeyAlias());
    }

    @NotNull
    private DapsPublicKeyProvider getDapsPublicKeyProvider( ConfigContainer configContainer,
                                                            ClientProvider clientProvider,
                                                            TokenManagerService tokenManagerService ) {
        DapsPublicKeyProvider dapsPublicKeyProvider =
                new TokenProviderService(configContainer, clientProvider, tokenManagerService);
        ReflectionTestUtils
                .setField(dapsPublicKeyProvider, "dapsTokenUrl", "https://daps.aisec.fraunhofer.de/v2/token");
        ReflectionTestUtils.setField(dapsPublicKeyProvider, "dapsKeyUrl",
                                     "https://daps.aisec.fraunhofer.de/.well-known/jwks.json");
        ReflectionTestUtils.setField(dapsPublicKeyProvider, "keyKid", "default");
        return dapsPublicKeyProvider;
    }


    public void testRemoveResourceFromBroker() {
    }

    public void testUpdateResourceAtBroker() {
    }

    public void testUnregisterAtBroker() {
    }

    public void testUpdateSelfDescriptionAtBroker() {
    }

    private MockResponse createMockResponse( String messageProcessedNotificationMessage )
            throws DatatypeConfigurationException, IOException {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        String body =
                new String(Files.readAllBytes(Paths.get("src/test/resources/MessageProcessedNotificationMessage.txt")));
        System.out.println(body);
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "multipart/form-data; boundary=QMFJydbI0FinMhohvQew_lJ03_vzXZ;charset=UTF-8")
                .addHeader("Transfer-Encoding", "chunked")
                .addHeader("Date", now)
                .addHeader("Keep-Alive", "timeout=60")
                .addHeader("Connection", "keep-alive")
                .setBody(body);
        return mockResponse;
    }

    public void testQueryBroker() {
    }



/*    public void setUp() throws Exception {
        final Serializer serializer = new Serializer();
        this.configurationContainer = new ConfigContainer(configurationModel, keyStoreManager);
        this.clientProvider = new ClientProvider(configurationContainer);
        configurationContainer.setClientProvider(clientProvider);
        TokenManagerService tokenManagerService = new AisecTokenManagerService(clientProvider, configurationContainer);
        this.dapsTokenProvider = getDapsTokenProvider(configurationContainer, clientProvider, tokenManagerService);
        DapsPublicKeyProvider dapsPublicKeyProvider =
                getDapsPublicKeyProvider(configurationContainer, clientProvider, tokenManagerService);
        DapsValidator dapsValidator = new DapsValidator(dapsPublicKeyProvider);
        this.idsHttpService = new IdsHttpService(clientProvider, dapsValidator, configurationContainer, serializer);

        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        this.brokerService = new BrokerService(configurationContainer, clientProvider, dapsTokenProvider, idsHttpService);

    }*/
}