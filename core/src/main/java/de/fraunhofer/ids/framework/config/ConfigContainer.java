package de.fraunhofer.ids.framework.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManagerInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ConfigurationContainer wraps the current configuration with the respective key- and truststore,
 * and manages changes of the configuration.
 */
public class ConfigContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigContainer.class);

    private ConfigurationModel configurationModel;
    private KeyStoreManager    keyStoreManager;
    private ClientProvider     clientProvider;

    /**
     * Create a ConfigurationContainer with a ConfigurationModel and KeyStoreManager
     *
     * @param configurationModel the initial {@link ConfigurationModel} of the Connector
     * @param keyStoreManager    the KeyStoreManager, managing Key- and Truststore of the Connector
     */
    public ConfigContainer( ConfigurationModel configurationModel, KeyStoreManager keyStoreManager ) {
        this.configurationModel = configurationModel;
        this.keyStoreManager = keyStoreManager;
    }

    /**
     * Setter for a {@link ClientProvider}
     *
     * @param provider the ClientProvider
     */
    public void setClientProvider( ClientProvider provider ) {
        this.clientProvider = provider;
    }

    /**
     * Getter for the {@link ConfigurationModel}
     *
     * @return the managed ConfigurationModel
     */
    public ConfigurationModel getConfigModel() {
        return this.configurationModel;
    }

    /**
     * Getter for the {@link Connector} (ConnectorDescription of the {@link ConfigurationModel})
     *
     * @return the ConnectorDescription of the managed ConfigurationModel
     */
    public Connector getConnector() {
        return this.configurationModel.getConnectorDescription();
    }

    /**
     * Getter for the {@link KeyStoreManager}
     *
     * @return the keymanager for Key- and Truststore defined by the ConfigurationModel
     */
    public KeyStoreManager getKeyManager() {
        return this.keyStoreManager;
    }

    /**
     * Update the ConfigurationContainer with a new {@link ConfigurationModel}, rebuild the KeyStoreManager with
     * new Configuration in the process
     *
     * @param configurationModel the new configurationModel that replaces the current one
     *
     * @throws ConfigUpdateException when the Key- and Truststore in the new Connector cannot be initialized
     */
    public void updateConfiguration( ConfigurationModel configurationModel ) throws ConfigUpdateException {
        try {
            LOGGER.debug("Updating the current configuration");
            var manager = rebuildKeyStoreManager(configurationModel);
            LOGGER.debug("KeyStoreManager rebuilt");
            this.configurationModel = configurationModel;
            this.keyStoreManager = manager;
            if( clientProvider != null ) {
                clientProvider.updateConfig();
                LOGGER.debug("ClientProvider updated!");
            }
        } catch( KeyStoreManagerInitializationException e ) {
            LOGGER.error("Configuration could not be updated! Keeping old configuration!");
            throw new ConfigUpdateException(e.getMessage(), e.getCause());
        } catch( NoSuchAlgorithmException | KeyManagementException e ) {
            LOGGER.error("New Key- or Truststore could not be initialized! Keeping old configuration!");
            LOGGER.error(e.getMessage(), e);
            throw new ConfigUpdateException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Rebuild the {@link KeyStoreManager} with a given configuration
     *
     * @param configurationModel the current ConfigurationModel
     *
     * @return the newly built KeyStoreManager
     *
     * @throws KeyStoreManagerInitializationException when the new KeyStoreManager cannot be initialized
     */
    private KeyStoreManager rebuildKeyStoreManager( ConfigurationModel configurationModel )
            throws KeyStoreManagerInitializationException {
        LOGGER.debug("Creating a new KeyStoreManager using current configuration");
        var keyPw = keyStoreManager.getKeyStorePw();
        var trustPw = keyStoreManager.getTrustStorePw();
        var alias = keyStoreManager.getKeyAlias();
        return new KeyStoreManager(configurationModel, keyPw, trustPw, alias);
    }

}