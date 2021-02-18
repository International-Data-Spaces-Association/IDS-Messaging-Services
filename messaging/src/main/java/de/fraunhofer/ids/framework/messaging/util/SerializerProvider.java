package de.fraunhofer.ids.framework.messaging.util;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provide a IAIS Infomodel {@link Serializer} as Bean for autowiring in spring applications.
 * The Serializer is used by various components inside the framework.
 */
@Configuration
public class SerializerProvider {

    /**
     * Infomodel Serializer as Bean, so it can be autowired
     *
     * @return a new {@link Serializer} object as bean for autowiring
     */
    @Bean
    public Serializer getSerializer() {
        return new Serializer();
    }

}
