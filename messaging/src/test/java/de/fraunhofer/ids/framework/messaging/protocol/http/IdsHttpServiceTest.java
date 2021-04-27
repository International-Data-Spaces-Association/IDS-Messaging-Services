package de.fraunhofer.ids.framework.messaging.protocol.http;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.DapsValidator;
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

@ContextConfiguration(classes = {IdsHttpServiceTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = { "shacl.validation=false" })
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
