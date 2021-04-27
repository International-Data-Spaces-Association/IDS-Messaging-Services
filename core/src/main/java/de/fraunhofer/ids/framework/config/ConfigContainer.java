package de.fraunhofer.ids.framework.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManagerInitializationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * The ConfigurationContainer wraps the current configuration with the respective key- and truststore,
 * and manages changes of the configuration.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigContainer {
    @Getter
    ConfigurationModel configurationModel;

    @Getter
    KeyStoreManager keyStoreManager;

    @Setter
    ClientProvider clientProvider;

    /**
     * Create a ConfigurationContainer with a ConfigurationModel and KeyStoreManager.
     *
     * @param configurationModel the initial {@link ConfigurationModel} of the Connector
     * @param keyStoreManager    the KeyStoreManager, managing Key- and Truststore of the Connector
     */
    public ConfigContainer(final ConfigurationModel configurationModel, final KeyStoreManager keyStoreManager) {
        this.configurationModel = configurationModel;
        this.keyStoreManager = keyStoreManager;
    }

    /**
     * Getter for the {@link Connector} (ConnectorDescription of the {@link ConfigurationModel}).
     *
     * @return the ConnectorDescription of the managed ConfigurationModel
     */
    public Connector getConnector() {
        return configurationModel.getConnectorDescription();
    }

    /**
     * Update the ConfigurationContainer with a new {@link ConfigurationModel}, rebuild the KeyStoreManager with
     * new Configuration in the process.
     *
     * @param configurationModel the new configurationModel that replaces the current one
     * @throws ConfigUpdateException when the Key- and Truststore in the new Connector cannot be initialized
     */
    public void updateConfiguration(final ConfigurationModel configurationModel) throws ConfigUpdateException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating the current configuration");
            }

            final var manager = rebuildKeyStoreManager(configurationModel);

            if (log.isDebugEnabled()) {
                log.debug("KeyStoreManager rebuilt");
            }

            this.configurationModel = configurationModel;
            this.keyStoreManager = manager;

            if (clientProvider != null) {
                clientProvider.updateConfig();
                if (log.isDebugEnabled()) {
                    log.debug("ClientProvider updated!");
                }
            }
        } catch (KeyStoreManagerInitializationException e) {
            if (log.isErrorEnabled()) {
                log.error("Configuration could not be updated! Keeping old configuration!");
            }

            throw new ConfigUpdateException(e.getMessage(), e.getCause());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            if (log.isErrorEnabled()) {
                log.error("New Key- or Truststore could not be initialized! Keeping old configuration!");
                log.error(e.getMessage(), e);
            }
            throw new ConfigUpdateException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Rebuild the {@link KeyStoreManager} with a given configuration.
     *
     * @param configurationModel the current ConfigurationModel
     * @return the newly built KeyStoreManager
     * @throws KeyStoreManagerInitializationException when the new KeyStoreManager cannot be initialized
     */
    private KeyStoreManager rebuildKeyStoreManager(final ConfigurationModel configurationModel)
            throws KeyStoreManagerInitializationException {
        if (log.isDebugEnabled()) {
            log.debug("Creating a new KeyStoreManager using current configuration");
        }

        final var keyPw = keyStoreManager.getKeyStorePw();
        final var trustPw = keyStoreManager.getTrustStorePw();
        final var alias = keyStoreManager.getKeyAlias();

        return new KeyStoreManager(configurationModel, keyPw, trustPw, alias);
    }
}
