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
package ids.messaging.endpoint;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@WebMvcTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = { "shacl.validation=false" })
class EndpointServiceTest {

    @Autowired
    private EndpointService endpointService;

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
