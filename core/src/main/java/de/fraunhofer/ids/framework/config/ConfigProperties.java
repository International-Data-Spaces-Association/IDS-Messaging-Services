package de.fraunhofer.ids.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Set of Properties to configure the imported configuration at startup.
 * Properties should be added to the application.properties file
 */
@Data
@ConfigurationProperties(prefix = "configuration")
public class ConfigProperties {

    /**
     * Path to a configuration File (JsonLD representation of a {@link de.fraunhofer.iais.eis.ConfigurationModel}.
     */
    private String path;
    /**
     * Password for the IDSKeystore configured in the {@link de.fraunhofer.iais.eis.ConfigurationModel} keyStore field.
     */
    private String keyStorePassword;
    /**
     * Alias of the connectors private key (used for signing DAT Requests).
     */
    private String keyAlias;
    /**
     * Password for the IDSTruststore configured in the {@link de.fraunhofer.iais.eis.ConfigurationModel} trustStore field.
     */
    private String trustStorePassword;
}
