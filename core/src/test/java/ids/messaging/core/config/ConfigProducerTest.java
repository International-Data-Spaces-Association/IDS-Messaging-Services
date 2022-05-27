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
package ids.messaging.core.config;

import java.net.URI;
import java.time.Duration;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import ids.messaging.core.daps.DapsConnectionException;
import ids.messaging.core.daps.DapsEmptyResponseException;
import ids.messaging.core.daps.aisec.AisecTokenManagerService;
import ids.messaging.core.daps.orbiter.OrbiterTokenManagerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestSpringApp.class)
@TestPropertySource(locations = "classpath:application.properties")
class ConfigProducerTest {
    @Autowired
    private ConfigContainer configContainer;

    @Autowired
    private ClientProvider clientProvider;

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
        //should be set, when configinterceptor sets it
        assertNotNull(configContainer.getConfigurationModel().getProperties().get("modified"));
        assertNotNull(configContainer.getConfigurationModel().getProperties().get("preInterceptor"));
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
        final var tokenManagerService = new AisecTokenManagerService(clientProvider, configContainer);
        assertEquals("INVALID_TOKEN", tokenManagerService.acquireToken("https://daps.aisec.fraunhofer.de/v2/token"));

        //orbiter DAPS currently not functional reachable
        //test will either throw IllegalArgumentException (URL not reachable) or JSONException (some HTML error code returned)
        //test will fail, if Orbiter DAPS reachable and working again
        final var orbiterTokenManagerService = new OrbiterTokenManagerService(clientProvider);
        assertThrows(Exception.class, () -> orbiterTokenManagerService.acquireToken("https://orbiter-daps-staging.truzzt.org/api/oauth/token"));
    }
}
