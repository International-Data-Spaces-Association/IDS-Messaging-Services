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
package de.fraunhofer.ids.messaging.protocol.http;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.messaging.core.config.ClientProvider;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.DapsValidator;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.Assert.assertEquals;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = { "shacl.validation=false" })
@ContextConfiguration(classes = {IdsHttpServiceTest.TestContextConfiguration.class})
class IdsHttpServiceTest {
    @Autowired
    IdsHttpService idsHttpService;

    @Autowired
    DapsValidator dapsValidator;

    @Autowired
    ConfigContainer configContainer;

    @Autowired
    ClientProvider clientProvider;

    @MockBean
    Connector connector;

    @MockBean
    ConfigurationModel configurationModel;

    private MockWebServer mockWebServer = new MockWebServer();

    @Configuration
    static class TestContextConfiguration {

        @MockBean
        DapsValidator dapsValidator;

        @MockBean
        ConfigContainer configContainer;

        @MockBean
        ClientProvider clientProvider;

        @Bean
        public IdsHttpService getIdsHttpService(){
            return new IdsHttpService(clientProvider, dapsValidator, configContainer, new Serializer());
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testHttpService() throws Exception {
        //configure Mockito
        Mockito.when(configContainer.getConnector()).thenReturn(connector);
        Mockito.when(configurationModel.getConnectorDescription()).thenReturn(connector);
        Mockito.when(clientProvider.getClient()).thenReturn(new OkHttpClient());
        //start the MockWebServer
        mockWebServer.start();
        //create a default response
        MockResponse mockResponse = new MockResponse().setBody("This is a response.").setResponseCode(200);
        mockWebServer.enqueue(mockResponse);
        //send a request using idsHttpService
        var response = idsHttpService.send("This is a Message.", mockWebServer.url("/").uri());
        //check if response body and MockWebServer response are equal
        assertEquals("This is a response.", response.body().string());
    }
}
