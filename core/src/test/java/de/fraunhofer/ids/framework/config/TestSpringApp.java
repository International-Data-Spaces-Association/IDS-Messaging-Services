package de.fraunhofer.ids.framework.config;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestSpringApp {

    public static void main(final String... args) {
        SpringApplication.run(TestSpringApp.class, args);
    }

    @Bean
    public Serializer getSerializer()
    {
        return new Serializer();
    }

}
