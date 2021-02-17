package de.fraunhofer.ids.framework.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManagerInitializationException;
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
 * Spring application.properties
 */
@Slf4j
@Configuration
@ConditionalOnClass( { ConfigurationModel.class, Connector.class, KeyStoreManager.class } )
@EnableConfigurationProperties( ConfigProperties.class )
public class ConfigProducer {
    private ConfigContainer configContainer;
    private ClientProvider  clientProvider;

    /**
     * Load the ConfigurationModel from the location specified in the application.properties, initialize the KeyStoreManager
     *
     * @param SERIALIZER an infomodel serializer for reading the jsonLD configuration
     * @param properties the {@link ConfigProperties} parsed from an application.properties file
     */
    public ConfigProducer( final Serializer SERIALIZER, ConfigProperties properties ) {
        try {
            log.debug(String.format("Loading configuration from %s", properties.getPath()));
            var config = getConfiguration(properties);

            log.info("Importing configuration from file");
            var configModel = SERIALIZER.deserialize(config, ConfigurationModel.class);

            //initialize the KeyStoreManager with Key and Truststore locations in the ConfigurationModel
            log.info("Initializing KeyStoreManager");
            var manager = new KeyStoreManager(configModel, properties.getKeyStorePassword().toCharArray(),
                                              properties.getTrustStorePassword().toCharArray(),
                                              properties.getKeyAlias());

            log.info("Imported existing configuration from file.");
            configContainer = new ConfigContainer(configModel, manager);

            log.info("Creating ClientProvider");
            clientProvider = new ClientProvider(configContainer);
            configContainer.setClientProvider(clientProvider);

        } catch( IOException e ) {
            log.error("Configuration cannot be parsed!");
            log.error(e.getMessage(), e);
        } catch( KeyStoreManagerInitializationException e ) {
            log.error("KeyStoreManager could not be initialized!");
            log.error(e.getMessage(), e);
        } catch( NoSuchAlgorithmException | KeyManagementException e ) {
            log.error("ClientProvider could not be initialized!");
            log.error(e.getMessage(), e);
        }
    }

    private String getConfiguration( ConfigProperties properties ) throws IOException {
        //load config jsonLD from given path
        if( Paths.get(properties.getPath()).isAbsolute() ) {
            return getAbsolutePathConfig(properties);
        } else {
            return getClassPathConfig(properties);
        }
    }

    private String getClassPathConfig( ConfigProperties properties ) throws IOException {
        log.info(String.format("Loading config from classpath: %s", properties.getPath()));
        var configurationStream = new ClassPathResource(properties.getPath()).getInputStream();
        var config = IOUtils.toString(configurationStream);
        configurationStream.close();
        return config;
    }

    private String getAbsolutePathConfig( ConfigProperties properties ) throws IOException {
        log.info(String.format("Loading config from absolute Path %s", properties.getPath()));
        var fis = new FileInputStream(properties.getPath());
        var config = IOUtils.toString(fis);
        fis.close();
        return config;
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
