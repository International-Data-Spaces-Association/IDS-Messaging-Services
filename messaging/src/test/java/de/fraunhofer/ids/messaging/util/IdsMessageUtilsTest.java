package de.fraunhofer.ids.messaging.util;

import java.net.URI;
import java.util.List;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.ConfigurationModelBuilder;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.ConnectorStatus;
import de.fraunhofer.iais.eis.LogLevel;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IdsMessageUtilsTest {

    @Test
    public void testUtils(){
        final var connector = new BaseConnectorBuilder()
                ._curator_(URI.create("http://curator"))
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("http://localhost")).build())
                ._inboundModelVersion_(List.of("1.0.0"))
                ._maintainer_(URI.create("http://maintainer"))
                ._outboundModelVersion_("1.0.0")
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                .build();

        final var configurationModel = new ConfigurationModelBuilder()
                ._configurationModelLogLevel_(LogLevel.DEBUG_LEVEL_LOGGING)
                ._connectorDeployMode_(ConnectorDeployMode.TEST_DEPLOYMENT)
                ._connectorStatus_(ConnectorStatus.CONNECTOR_OFFLINE)
                ._connectorDescription_(connector)
                .build();

        assertNotNull(IdsMessageUtils.asList(1,2,3));
        assertDoesNotThrow(() -> new Serializer().deserialize(IdsMessageUtils.buildSelfDeclaration(connector), BaseConnector.class));
        assertDoesNotThrow(() -> new Serializer().deserialize(IdsMessageUtils.buildSelfDeclaration(configurationModel), BaseConnector.class));
    }
}
