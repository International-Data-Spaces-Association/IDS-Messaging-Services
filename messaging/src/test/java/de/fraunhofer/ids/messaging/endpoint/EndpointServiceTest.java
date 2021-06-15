package de.fraunhofer.ids.messaging.endpoint;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.dispatcher.MessageDispatcherProvider;
import de.fraunhofer.ids.messaging.handler.request.RequestMessageHandlerService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@FieldDefaults(level = AccessLevel.PRIVATE)
@TestPropertySource(properties = { "shacl.validation=false" })
class EndpointServiceTest {

    @Autowired
    EndpointService endpointService;

    @Configuration
    static class TestContextConfiguration{
        @Autowired
        RequestMappingHandlerMapping mappingHandlerMapping;

        @MockBean
        MessageController messageController;

        @Bean
        public Serializer getSerializer(){
            return new Serializer();
        }

        @Bean
        public EndpointService getEndpointService(){return new EndpointService(messageController, mappingHandlerMapping);}
    }

    @Test
    void testService(){
        endpointService.addMapping("/api/ids/something");
        endpointService.removeMapping("/api/ids/something");
    }
}