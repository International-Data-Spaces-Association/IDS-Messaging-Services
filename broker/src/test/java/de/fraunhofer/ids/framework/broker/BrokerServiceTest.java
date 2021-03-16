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
import de.fraunhofer.ids.framework.messaging.protocol.http.IdsHttpService;
import junit.framework.TestCase;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static java.lang.ClassLoader.getSystemResource;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@EnableConfigurationProperties(value = ConfigProperties.class)
public class BrokerServiceTest {
   @Autowired
    private ConfigurationModel configModel;

    @Autowired
    private Connector connector;

    @Autowired
    private KeyStoreManager keyStoreManager;

    @MockBean
    private ConfigurationModel configurationModel;

    private ConfigContainer   configurationContainer = mock(ConfigContainer.class);

    @MockBean
    private ClientProvider    clientProvider;

    private DapsTokenProvider dapsTokenProvider = mock(DapsTokenProvider.class);

    @MockBean
    private DapsPublicKeyProvider dapsPublicKeyProvider;

    @MockBean
    private DapsValidator dapsValidator;

    @MockBean
    private IdsHttpService    idsHttpService;

    private MockWebServer     mockWebServer;

    private BrokerService     brokerService;


    @Test
    public void testUpdateSelfDescriptionAtBrokers ()throws Exception {
        DynamicAttributeToken fakeToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_("fake Token")
                .build();
        Mockito.when(configurationContainer.getConnector()).thenReturn(connector);
        Mockito.when(connector.getId()).thenReturn(new URL("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5").toURI());
        //Mockito.when(connector.getOutboundModelVersion()).thenReturn("1.0.3");
        //Mockito.when(configurationContainer.getConfigurationModel()).thenReturn(configurationModel);
        //Mockito.when(configurationModel.getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
        //Mockito.when(dapsTokenProvider.provideDapsToken()).thenReturn("Mocked Token.");
        //Mockito.when(dapsPublicKeyProvider.providePublicKey()).thenReturn(null);
        //Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        //Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsTokenProvider.getDAT()).thenReturn(fakeToken);
        this.brokerService = new BrokerService(configurationContainer, clientProvider, dapsTokenProvider, idsHttpService);
        this.mockWebServer = new MockWebServer();
        MockResponse mockResponse = createMockResponse("MessageProcessedNotificationMessage");
        this.mockWebServer.enqueue(mockResponse);
        Response response = this.brokerService.updateSelfDescriptionAtBroker(mockWebServer.url("/").toString());
        System.out.println(response.body().string());
        assertTrue(response.isSuccessful());
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
    }*/


   public void setUp() throws Exception {
        final Serializer serializer = new Serializer();
        this.configContainer = new ConfigContainer(configModel, keyStoreManager);
        this.clientProvider = new ClientProvider(configContainer);
        configContainer.setClientProvider(clientProvider);
        TokenManagerService tokenManagerService = new AisecTokenManagerService(clientProvider, configContainer);
        this.dapsTokenProvider = getDapsTokenProvider(configContainer, clientProvider, tokenManagerService);
        DapsPublicKeyProvider dapsPublicKeyProvider =
                getDapsPublicKeyProvider(configContainer, clientProvider, tokenManagerService);
        DapsValidator dapsValidator = new DapsValidator(dapsPublicKeyProvider);
        this.idsHttpService = new IdsHttpService(clientProvider, dapsValidator, configContainer, serializer);

        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        this.brokerService = new BrokerService(configContainer, clientProvider, dapsTokenProvider, idsHttpService);

    }
}