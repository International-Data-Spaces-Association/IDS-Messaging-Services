package de.fraunhofer.ids.messaging.core.config;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import de.fraunhofer.iais.eis.BasicAuthenticationBuilder;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ProxyBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.messaging.core.daps.DapsConnectionException;
import de.fraunhofer.ids.messaging.core.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.messaging.core.daps.TokenManagerService;
import de.fraunhofer.ids.messaging.core.daps.aisec.AisecTokenManagerService;
import de.fraunhofer.ids.messaging.core.daps.orbiter.OrbiterTokenManagerService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
@SpringBootTest(classes = TestSpringApp.class)
@TestPropertySource(locations = "classpath:application.properties")
class ConfigProducerTest {
    //TODO check through .p12 files in test resources, replace everything there with localhost certificates just for testing

    @Autowired
    ConfigContainer configContainer;

    @Autowired
    ClientProvider clientProvider;

    /**
     * SpringBootTest will initialize all Components in Core using the test/resources/application.properties file
     * then the test will check some fields of the loaded config.json of the imported configurationModel
     */
    @Test
    void testLoadInitialConfiguration() {
        final var configModel = configContainer.getConfigurationModel();
        assertEquals(URI.create("https://w3id.org/idsa/autogen/configurationModel/a0b8bcea-6ba0-4e26-ba80-44e43ee058ac"), configModel.getId());
        assertEquals(ConnectorDeployMode.TEST_DEPLOYMENT, configModel.getConnectorDeployMode());
        assertEquals(URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"),configModel.getConnectorDescription().getId());
        assertNotNull(clientProvider.getClient());
        assertNotNull(configContainer.getKeyStoreManager().getCert());
        assertNotNull(configContainer.getKeyStoreManager().getTrustManager());
        assertDoesNotThrow(() -> configContainer.updateConfiguration(configModel));
    }

    @Test
    void testClientProvider(){
        assertNotNull(clientProvider.getClient());
        assertNotNull(clientProvider.getClientWithTimeouts(Duration.ofMillis(10), Duration.ofMillis(10),Duration.ofMillis(10),Duration.ofMillis(10)));
        assertNotNull(clientProvider.getClientWithTimeouts(null, null, null, null));
    }

    @Test
    void testProvider() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        var tokenManagerService = new AisecTokenManagerService(clientProvider, configContainer);
        assertEquals("INVALID_TOKEN", tokenManagerService.acquireToken("https://daps.aisec.fraunhofer.de/v2/token"));

        //orbiter DAPS currently not reachable (and currently not added to test connectors NO_PROXY), so this should throw an IllegalArgumentException
        var orbiterTokenManagerService = new OrbiterTokenManagerService(clientProvider);
        assertThrows(IllegalArgumentException.class, () -> orbiterTokenManagerService.acquireToken("https://orbiter-daps-staging.truzzt.org/api/oauth/token"));
    }
}
