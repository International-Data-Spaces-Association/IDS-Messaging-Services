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
package ids.messaging.core.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import ids.messaging.core.config.ssl.keystore.KeyStoreManagerInitializationException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The ConfigurationContainer wraps the current configuration with the respective key- and
 * truststore, and manages changes of the configuration.
 */
@Slf4j
public class ConfigContainer {
    /**
     * The ConfigurationModel.
     */
    @Getter
    private ConfigurationModel configurationModel;

    /**
     * The KeyStoreManager.
     */
    @Getter
    private KeyStoreManager keyStoreManager;

    /**
     * The ClientProvider.
     */
    @Setter
    private ClientProvider clientProvider;

    /**
     * Create a ConfigurationContainer with a ConfigurationModel and KeyStoreManager.
     *
     * @param configurationModel The initial {@link ConfigurationModel} of the Connector.
     * @param keyStoreManager The KeyStoreManager, managing Key- and Truststore of the Connector.
     */
    public ConfigContainer(final ConfigurationModel configurationModel,
                           final KeyStoreManager keyStoreManager) {
        this.configurationModel = configurationModel;
        this.keyStoreManager = keyStoreManager;
    }

    /**
     * Getter for the {@link Connector} (ConnectorDescription of the {@link ConfigurationModel}).
     *
     * @return The ConnectorDescription of the managed ConfigurationModel.
     */
    public Connector getConnector() {
        return configurationModel.getConnectorDescription();
    }

    /**
     * Update the ConfigurationContainer with a new {@link ConfigurationModel},
     * rebuild the KeyStoreManager with new Configuration in the process.
     *
     * @param configurationModel The new configurationModel that replaces the current one.
     * @throws ConfigUpdateException When the Key- and Truststore in the new Connector
     * cannot be initialized.
     */
    public void updateConfiguration(final ConfigurationModel configurationModel)
            throws ConfigUpdateException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating the current configuration... [code=(IMSCOD0083)]");
            }

            final var manager = rebuildKeyStoreManager(configurationModel);

            if (log.isDebugEnabled()) {
                log.debug("KeyStoreManager rebuilt. [code=(IMSCOD0084)]");
            }

            this.configurationModel = configurationModel;
            this.keyStoreManager = manager;

            if (clientProvider != null) {
                clientProvider.updateConfig();
                if (log.isDebugEnabled()) {
                    log.debug("ClientProvider updated! [code=(IMSCOD0085)]");
                }
            }
        } catch (KeyStoreManagerInitializationException e) {
            if (log.isErrorEnabled()) {
                log.error("Configuration could not be updated!"
                    + " Keeping old configuration! [code=(IMSCOE0003), exception=({})]",
                    e.getMessage());
            }

            throw new ConfigUpdateException(e.getMessage(), e.getCause());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            if (log.isErrorEnabled()) {
                log.error("New Key- or Truststore could not be initialized!"
                    + " Keeping old configuration! [code=(IMSCOE0004),"
                    + " exception=({})]", e.getMessage());
            }
            throw new ConfigUpdateException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Rebuild the {@link KeyStoreManager} with a given configuration.
     *
     * @param configurationModel The current ConfigurationModel.
     * @return The newly built KeyStoreManager.
     * @throws KeyStoreManagerInitializationException When the new KeyStoreManager cannot be
     * initialized.
     */
    private KeyStoreManager rebuildKeyStoreManager(final ConfigurationModel configurationModel)
            throws KeyStoreManagerInitializationException {
        if (log.isDebugEnabled()) {
            log.debug("Creating a new KeyStoreManager using current(!) configuration..."
                      + " [code=(IMSCOD0086)]");
        }

        final var keyPw = keyStoreManager.getKeyStorePw();
        final var trustPw = keyStoreManager.getTrustStorePw();
        final var alias = keyStoreManager.getKeyAlias();

        return new KeyStoreManager(configurationModel, keyPw, trustPw, alias);
    }
}
