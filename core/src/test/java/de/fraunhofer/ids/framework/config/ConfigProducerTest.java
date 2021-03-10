package de.fraunhofer.ids.framework.config;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestSpringApp.class)
@TestPropertySource(locations = "classpath:application.properties")
class ConfigProducerTest {

    @Autowired
    private ConfigContainer configContainer;

    /**
     * SpringBootTest will initialize all Components in Core using the test/resources/application.properties file
     * then the test will check some fields of the loaded config.json of the imported configurationModel
     */
    @Test
    public void testLoadInitialConfiguration(){
        var configModel = configContainer.getConfigurationModel();
        assertEquals(configModel.getId(), URI.create("https://w3id.org/idsa/autogen/configurationModel/a0b8bcea-6ba0-4e26-ba80-44e43ee058ac"));
        assertEquals(configModel.getConnectorDeployMode(), ConnectorDeployMode.TEST_DEPLOYMENT);
        assertEquals(configModel.getConnectorDescription().getId(), URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"));
    }

}