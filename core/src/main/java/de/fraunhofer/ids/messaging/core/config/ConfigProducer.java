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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.messaging.core.config.ssl.keystore.KeyStoreManagerInitializationException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Parse the configuration and initialize the key- and truststores specified in the {@link ConfigProperties} via
 * Spring application.properties.
 */
@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableConfigurationProperties(ConfigProperties.class)
@ConditionalOnClass({ConfigurationModel.class, Connector.class, KeyStoreManager.class})
public class ConfigProducer {
    static final Serializer SERIALIZER = new Serializer();

    ConfigContainer configContainer;
    ClientProvider  clientProvider;

    /**
     * Load the ConfigurationModel from the location specified in the application.properties, initialize the KeyStoreManager.
     *
     * @param properties the {@link ConfigProperties} parsed from an application.properties file
     */
    public ConfigProducer(final ConfigProperties properties,
                          Optional<PreConfigProducerInterceptor> preInterceptor,
                          Optional<PostConfigProducerInterceptor> postInterceptor) {

        ConfigurationModel configModel = null;

        if(preInterceptor.isPresent()){
            try {
                configModel = preInterceptor.get().perform(properties);
            } catch (ConfigProducerInterceptorException e) {
                if(log.isErrorEnabled()) {
                    log.error("PreConfigProducerInterceptor failed! " + e.getMessage());
                }
            }
        }else{
            try {
                configModel = loadConfig(properties);
                if (log.isInfoEnabled()) {
                    log.info("Imported existing configuration from file.");
                }
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Configuration cannot be parsed! " + e.getMessage());
                }
            }
        }

        if(configModel != null) {
            try {
                if (log.isInfoEnabled()) {
                    log.info("Initializing KeyStoreManager");
                }
                //initialize the KeyStoreManager with Key and Truststore locations in the ConfigurationModel
                final var manager = new KeyStoreManager(configModel, properties.getKeyStorePassword().toCharArray(),
                        properties.getTrustStorePassword().toCharArray(),
                        properties.getKeyAlias());

                configContainer = new ConfigContainer(configModel, manager);

                if (log.isInfoEnabled()) {
                    log.info("Creating ClientProvider");
                }
                clientProvider = new ClientProvider(configContainer);
                configContainer.setClientProvider(clientProvider);

                postInterceptor.ifPresent(
                        interceptor -> {
                            try {
                                interceptor.perform(configContainer);
                            } catch (ConfigProducerInterceptorException e) {
                                if (log.isErrorEnabled()) {
                                    log.error("PreConfigProducerInterceptor failed! " + e.getMessage());
                                }
                            }
                        }
                );

            } catch (KeyStoreManagerInitializationException e) {
                if (log.isErrorEnabled()) {
                    log.error("KeyStoreManager could not be initialized! " + e.getMessage());
                }
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                if (log.isErrorEnabled()) {
                    log.error("ClientProvider could not be initialized! " + e.getMessage());
                }
            }
        }
    }

    private ConfigurationModel loadConfig(ConfigProperties properties) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Loading configuration from %s", properties.getPath()));
        }

        final var config = getConfiguration(properties);

        if (log.isInfoEnabled()) {
            log.info("Importing configuration from file");
        }

        return SERIALIZER.deserialize(config, ConfigurationModel.class);
    }

    private String getConfiguration(final ConfigProperties properties) throws IOException {
        if (Paths.get(properties.getPath()).isAbsolute()) {
            return getAbsolutePathConfig(properties);
        } else {
            return getClassPathConfig(properties);
        }
    }

    private String getClassPathConfig(final ConfigProperties properties) throws IOException {
        if (log.isInfoEnabled()) {
            log.info(String.format("Loading config from classpath: %s", properties.getPath()));
        }

        final var configurationStream = new ClassPathResource(properties.getPath()).getInputStream();
        final var config = IOUtils.toString(configurationStream);
        configurationStream.close();

        return config;
    }

    private String getAbsolutePathConfig(final ConfigProperties properties) throws IOException {
        if (log.isInfoEnabled()) {
            log.info(String.format("Loading config from absolute Path %s", properties.getPath()));
        }

        final var fis = new FileInputStream(properties.getPath());
        final var config = IOUtils.toString(fis);
        fis.close();

        return config;
    }

    /**
     * Provide the ConfigurationContainer as Bean for autowiring.
     *
     * @return the imported {@link ConfigurationModel} as bean for autowiring
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigContainer getConfigContainer() {
        return configContainer;
    }

    /**
     * Provide the ClientProvider as bean for autowiring.
     *
     * @return the created {@link ClientProvider} as bean for autowiring
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientProvider getClientProvider() {
        return clientProvider;
    }
}
