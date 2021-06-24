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

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Set of Properties to configure the imported configuration at startup.
 * Properties should be added to the application.properties file
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "configuration")
public class ConfigProperties {

    /**
     * Path to a configuration File (JsonLD representation of a {@link de.fraunhofer.iais.eis.ConfigurationModel}.
     */
    String path;
    /**
     * Password for the IDSKeystore configured in the {@link de.fraunhofer.iais.eis.ConfigurationModel} keyStore field.
     */
    String keyStorePassword;
    /**
     * Alias of the connectors private key (used for signing DAT Requests).
     */
    String keyAlias;
    /**
     * Password for the IDSTruststore configured in the {@link de.fraunhofer.iais.eis.ConfigurationModel} trustStore field.
     */
    String trustStorePassword;
}
