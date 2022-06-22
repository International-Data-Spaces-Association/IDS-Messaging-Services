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
 *       Fraunhofer Institute for Transportation and Infrastructure Systems
 *
 */
package ids.messaging.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Set of Properties to configure the imported configuration at startup.
 * Properties should be added to the application.properties file.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "configuration")
public class ConfigProperties {
    /**
     * Path to a configuration File (JsonLD representation of
     * a {@link de.fraunhofer.iais.eis.ConfigurationModel}.
     */
    @NotNull
    private String path;

    /**
     * Password for the IDSKeystore configured in the
     * {@link de.fraunhofer.iais.eis.ConfigurationModel} keyStore field.
     */

    @NotNull
    private String keyStorePassword;

    /**
     * Alias of the connectors private key (used for signing DAT Requests).
     */
    @NotNull
    private String keyAlias;

    /**
     * Password for the IDSTruststore configured in the
     * {@link de.fraunhofer.iais.eis.ConfigurationModel} trustStore field.
     */
    @NotNull
    private String trustStorePassword;
}
