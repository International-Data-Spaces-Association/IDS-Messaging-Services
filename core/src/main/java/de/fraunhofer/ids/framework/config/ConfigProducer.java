package de.fraunhofer.ids.framework.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManagerInitializationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Parse the configuration and initialize the key- and truststores specified in the {@link ConfigProperties} via
 * Spring application.properties
 */
@Configuration
@ConditionalOnClass( { ConfigurationModel.class, Connector.class, KeyStoreManager.class } )
@EnableConfigurationProperties( ConfigProperties.class )
public class ConfigProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigProducer.class);

    private ConfigContainer configContainer;

    private ClientProvider clientProvider;

    /**
     * Load the ConfigurationModel from the location specified in the application.properties, initialize the KeyStoreManager
     *
     * @param serializer an infomodel serializer for reading the jsonLD configuration
     * @param properties the {@link ConfigProperties} parsed from an application.properties file
     */
    public ConfigProducer( final Serializer serializer, ConfigProperties properties ) {
        try {
            LOGGER.debug(String.format("Loading configuration from %s", properties.getPath()));
            String config;
            //load config jsonLD from given path
            if( Paths.get(properties.getPath()).isAbsolute() ) {
                LOGGER.info(String.format("Loading config from absolute Path %s", properties.getPath()));
                var fis = new FileInputStream(properties.getPath());
                config = IOUtils.toString(fis);
                fis.close();
            } else {
                LOGGER.info(String.format("Loading config from classpath: %s", properties.getPath()));
                InputStream configurationStream = new ClassPathResource(properties.getPath()).getInputStream();
                config = IOUtils.toString(configurationStream);
                configurationStream.close();
            }
            LOGGER.info("Importing configuration from file");
            //deserialize to ConfigurationModel
            var configModel = serializer.deserialize(config, ConfigurationModel.class);
            LOGGER.info("Initializing KeyStoreManager");
            //initialize the KeyStoreManager with Key and Truststore locations in the ConfigurationModel
            var manager = new KeyStoreManager(configModel, properties.getKeyStorePassword().toCharArray(),
                                              properties.getTrustStorePassword().toCharArray(),
                                              properties.getKeyAlias());
            LOGGER.info("Imported existing configuration from file.");
            configContainer = new ConfigContainer(configModel, manager);
            LOGGER.info("Creating ClientProvider");
            //create a ClientProvider
            clientProvider = new ClientProvider(configContainer);
            configContainer.setClientProvider(clientProvider);
        } catch( IOException e ) {
            LOGGER.error("Configuration cannot be parsed!");
            LOGGER.error(e.getMessage(), e);
        } catch( KeyStoreManagerInitializationException e ) {
            LOGGER.error("KeyStoreManager could not be initialized!");
            LOGGER.error(e.getMessage(), e);
        } catch( NoSuchAlgorithmException | KeyManagementException e ) {
            LOGGER.error("ClientProvider could not be initialized!");
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Provide the ConfigurationContainer as Bean for autowiring
     *
     * @return the imported {@link ConfigurationModel} as bean for autowiring
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigContainer getConfigContainer() {
        return configContainer;
    }

    /**
     * Provide the ClientProvider as bean for autowiring
     *
     * @return the created {@link ClientProvider} as bean for autowiring
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientProvider getClientProvider() {
        return clientProvider;
    }

}
