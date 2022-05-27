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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import ids.messaging.core.config.ssl.keystore.KeyStoreManagerInitializationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Parse the configuration and initialize the key- and truststores specified in
 * the {@link ConfigProperties} via Spring application.properties.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
@ConditionalOnClass({ConfigurationModel.class, Connector.class, KeyStoreManager.class})
public class ConfigProducer {

    /**
     * Infomodel serializer.
     */
    private static final Serializer SERIALIZER = new Serializer();

    /**
     * The ConfigContainer.
     */
    private ConfigContainer configContainer;

    /**
     * The ClientProvider.
     */
    private ClientProvider  clientProvider;

    /**
     * Load the ConfigurationModel from the location specified in the
     * application.properties, initialize the KeyStoreManager.
     *
     * @param properties The {@link ConfigProperties} parsed from an application.properties file.
     * @param postInterceptor Possibility to add a post processing interceptor.
     * @param preInterceptor Possibility to add a pre processing interceptor.
     */
    public ConfigProducer(final ConfigProperties properties,
                          final Optional<PreConfigProducerInterceptor> preInterceptor,
                          final Optional<PostConfigProducerInterceptor> postInterceptor) {

        ConfigurationModel configModel = null;

        if (preInterceptor.isPresent()) {
            try {
                configModel = preInterceptor.get().perform(properties);
            } catch (ConfigProducerInterceptorException e) {
                if (log.isErrorEnabled()) {
                    log.error("PreConfigProducerInterceptor failed! [code=(IMSCOE0005),"
                              + " exception=({})]",
                              e.getMessage());
                }
            }
        } else {
            try {
                configModel = loadConfig(properties);
                if (log.isInfoEnabled()) {
                    log.info("Successfully imported configuration. [code=(IMSCOI0048)]");
                }
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Configuration cannot be parsed! [code=(IMSCOE0006), exception=({})]",
                              e.getMessage());
                }
            }
        }

        if (configModel != null) {
            try {
                //initialize the KeyStoreManager with Key and Truststore
                //locations in the ConfigurationModel
                final var manager = new KeyStoreManager(configModel, properties
                        .getKeyStorePassword().toCharArray(),
                        properties.getTrustStorePassword().toCharArray(),
                        properties.getKeyAlias());

                configContainer = new ConfigContainer(configModel, manager);
                clientProvider = new ClientProvider(configContainer);
                configContainer.setClientProvider(clientProvider);

                postInterceptor.ifPresent(
                        interceptor -> {
                            try {
                                interceptor.perform(configContainer);
                            } catch (ConfigProducerInterceptorException e) {
                                if (log.isErrorEnabled()) {
                                    log.error(
                                        "PreConfigProducerInterceptor failed! [code=(IMSCOE0007),"
                                        + " exception=({})]", e.getMessage());
                                }
                            }
                        }
                );

            } catch (KeyStoreManagerInitializationException e) {
                if (log.isErrorEnabled()) {
                    log.error("KeyStoreManager could not be initialized! [code=(IMSCOE0008),"
                              + " exception=({})]", e.getMessage());
                }
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                if (log.isErrorEnabled()) {
                    log.error("ClientProvider could not be initialized! [code=(IMSCOE0009),"
                              + " exception=({})]", e.getMessage());
                }
            }
        }
    }

    private ConfigurationModel loadConfig(final ConfigProperties properties) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Loading configuration... [code=(IMSCOD0087), path=({})]",
                      properties.getPath());
        }

        final var config = getConfiguration(properties);

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
        if (log.isDebugEnabled()) {
            log.debug("Loading configuration from classpath. [code=(IMSCOD0088), path=({})]",
                      properties.getPath());
        }

        final var configurationStream =
                new ClassPathResource(properties.getPath()).getInputStream();
        final var config = IOUtils.toString(configurationStream);
        configurationStream.close();

        return config;
    }

    private String getAbsolutePathConfig(final ConfigProperties properties) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Loading config from absolute Path. [code=(IMSCOD0089), path=({})]",
                      properties.getPath());
        }

        final var fis = new FileInputStream(properties.getPath());
        final var config = IOUtils.toString(fis);
        fis.close();

        return config;
    }

    /**
     * Provide the ConfigurationContainer as Bean for autowiring.
     *
     * @return The imported {@link ConfigurationModel} as bean for autowiring.
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigContainer getConfigContainer() {
        return configContainer;
    }

    /**
     * Provide the ClientProvider as bean for autowiring.
     *
     * @return The created {@link ClientProvider} as bean for autowiring.
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientProvider getClientProvider() {
        return clientProvider;
    }
}
