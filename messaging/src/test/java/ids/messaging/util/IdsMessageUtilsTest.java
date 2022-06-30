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
package ids.messaging.util;

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
    void testUtils(){
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
