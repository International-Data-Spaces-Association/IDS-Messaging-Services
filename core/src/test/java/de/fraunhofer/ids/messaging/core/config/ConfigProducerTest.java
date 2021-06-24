/*
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
 */
package de.fraunhofer.ids.messaging.core.config;

import java.net.URI;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void testLoadInitialConfiguration(){
        final var configModel = configContainer.getConfigurationModel();
        assertEquals(URI.create("https://w3id.org/idsa/autogen/configurationModel/a0b8bcea-6ba0-4e26-ba80-44e43ee058ac"), configModel.getId());
        assertEquals(ConnectorDeployMode.TEST_DEPLOYMENT, configModel.getConnectorDeployMode());
        assertEquals(URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"),configModel.getConnectorDescription().getId());
        assertNotNull(clientProvider.getClient());
        assertNotNull(configContainer.getKeyStoreManager().getCert());
        assertNotNull(configContainer.getKeyStoreManager().getTrustManager());
    }

}
