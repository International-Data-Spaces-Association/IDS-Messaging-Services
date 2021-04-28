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
