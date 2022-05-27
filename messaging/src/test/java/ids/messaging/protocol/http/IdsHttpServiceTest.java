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
package ids.messaging.protocol.http;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.core.config.ClientProvider;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsValidator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Mockito.when(clientProvider.getClientWithTimeouts(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(
                        new OkHttpClient.Builder()
                                .callTimeout(Duration.ofMillis(10))
                                .writeTimeout(Duration.ofMillis(10))
                                .readTimeout(Duration.ofMillis(10))
                                .connectTimeout(Duration.ofMillis(10))
                                .build()
                );
        //start the MockWebServer
        mockWebServer.start();
        //create a default response
        MockResponse mockResponse = new MockResponse().setBody("This is a response.").setResponseCode(200);
        mockWebServer.enqueue(mockResponse);
        //send a request using idsHttpService
        var response = idsHttpService.send("This is a Message.", mockWebServer.url("/").uri());
        mockWebServer.enqueue(mockResponse);
        assertNotNull(idsHttpService.get(mockWebServer.url("/").uri()));
        mockWebServer.enqueue(mockResponse);
        assertNotNull(idsHttpService.getWithHeaders(mockWebServer.url("/").uri(), Map.of("header", "payload")));
        mockWebServer.enqueue(mockResponse);
        assertNotNull(idsHttpService.sendWithHeaders(RequestBody.create("String", MediaType.parse("text/plain")), mockWebServer.url("/").uri(), Map.of("header", "payload")));
        idsHttpService.setTimeouts(Duration.ofMillis(10), Duration.ofMillis(10), Duration.ofMillis(10), Duration.ofMillis(10));
        mockWebServer.enqueue(mockResponse.setBodyDelay(10, TimeUnit.SECONDS).setHeadersDelay(10, TimeUnit.SECONDS));
        assertThrows(IOException.class, () -> idsHttpService.get(mockWebServer.url("/").uri()));
        idsHttpService.removeTimeouts();
        //check if response body and MockWebServer response are equal
        assertEquals("This is a response.", response.body().string());
    }
}
