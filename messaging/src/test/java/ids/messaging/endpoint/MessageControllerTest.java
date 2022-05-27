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

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.NotificationMessageBuilder;
import de.fraunhofer.iais.eis.RequestMessageBuilder;
import de.fraunhofer.iais.eis.ResponseMessage;
import de.fraunhofer.iais.eis.ResponseMessageBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.dispatcher.MessageDispatcher;
import ids.messaging.protocol.multipart.parser.MultipartParser;
import ids.messaging.response.BodyResponse;
import ids.messaging.util.IdsMessageUtils;
import ids.messaging.util.ResourceIDGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MessageController.class, MessageControllerTest.TestContextConfiguration.class})
class MessageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageController idsController;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private Serializer serializer;

    @MockBean
    private MessageDispatcher messageDispatcher;

    @MockBean
    private ConfigContainer configurationContainer;

    @MockBean
    private Connector connector;

    @MockBean
    private ConfigurationModel configurationModel;

    @MockBean
    private DapsTokenProvider provider;

    @Configuration
    static class TestContextConfiguration{
        @Bean
        public Serializer getSerializer(){
            return new Serializer();
        }
    }

    @Test
    void testGetValidMultipartReturn() throws Exception {
        final var requestMappingInfo = RequestMappingInfo
                .paths("/api/ids/data")
                .methods(RequestMethod.POST)
                .consumes(MediaType.MULTIPART_FORM_DATA_VALUE)
                .produces(MediaType.MULTIPART_FORM_DATA_VALUE)
                .build();
        requestMappingHandlerMapping.registerMapping(requestMappingInfo, idsController, MessageController.class.getDeclaredMethod("handleIDSMessage", HttpServletRequest.class));

        Mockito.when(configurationContainer.getConfigurationModel()).thenReturn(configurationModel);
        Mockito.when(configurationModel.getConnectorDescription()).thenReturn(connector);
        Mockito.when(configurationContainer.getConnector()).thenReturn(connector);
        Mockito.when(connector.getId()).thenReturn(new URL("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5").toURI());
        Mockito.when(connector.getOutboundModelVersion()).thenReturn("1.0.3");

        Mockito.doReturn(new ArrayList<>(List.of("*.*.*"))).when(connector).getInboundModelVersion();
        Mockito.when(provider.provideDapsToken()).thenReturn("Mocked Token.");

        // Create the message header that shall be send and tested
        //final var queryHeader = new RequestMessageBuilder()._modelVersion_("1.0.3")._issued_(Util.getGregorianNow()).build();
        final var token = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_("Token")
                .build();
        final var msgHeader = new RequestMessageBuilder(ResourceIDGenerator.randomURI(MessageControllerTest.class))
                ._issuerConnector_(configurationContainer.getConnector().getId())
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._securityToken_(token)
                ._senderAgent_(configurationContainer.getConnector().getId())
                ._modelVersion_(configurationContainer.getConnector().getOutboundModelVersion())
                .build();

        final var notificationHeader = new NotificationMessageBuilder(ResourceIDGenerator.randomURI(MessageControllerTest.class))
                ._issuerConnector_(configurationContainer.getConnector().getId())
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._securityToken_(token)
                ._senderAgent_(configurationContainer.getConnector().getId())
                ._modelVersion_(configurationContainer.getConnector().getOutboundModelVersion())
                .build();

        // Define mocking behaviour of the messageDispatcher.process() as well as the connector.getSelfDeclarationURL()
        final var mockResponseBody = "mock response";
        final var responseMessage = new ResponseMessageBuilder()
                ._correlationMessage_(msgHeader.getId())
                ._issuerConnector_(configurationContainer.getConnector().getId())
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._securityToken_(token)
                ._senderAgent_(configurationContainer.getConnector().getId())
                ._modelVersion_(configurationContainer.getConnector().getOutboundModelVersion()).build();

        Mockito.when(messageDispatcher.process(Mockito.any(), Mockito.any())).thenReturn(
                BodyResponse.create(responseMessage, mockResponseBody));

        final var header = serializer.serialize(msgHeader);
        final var header2 = serializer.serialize(notificationHeader);

        // Create the message payload that shall be send and tested
        final var queryPayload = "Some Payload";

        // Build a request Object with the target and the message header and payload as multipart request
        final var requestBuilder = MockMvcRequestBuilders.multipart("/api/ids/data")
                .part(new MockPart("header", header.getBytes()))
                .part(new MockPart("payload", queryPayload.getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.MULTIPART_FORM_DATA);

        // perform the request
        final var result = mockMvc
                .perform(requestBuilder)
                .andExpect(status().is(200))
                .andReturn();

        // Build a request Object with the target and the message header and payload as multipart request
        final var notificationRequestBuilder = MockMvcRequestBuilders.multipart("/api/ids/data")
                .part(new MockPart("header", header2.getBytes()))
                .part(new MockPart("payload", queryPayload.getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.MULTIPART_FORM_DATA);

        // perform the request
        final var notificationResult = mockMvc
                .perform(notificationRequestBuilder)
                .andExpect(status().is(200))
                .andReturn();

        final var response = result.getResponse();
        final var multiPartResp = MultipartParser.stringToMultipart(response.getContentAsString());
        final var respHead = multiPartResp.get("header");//.replaceAll("UTC","+0000");

        final var responseHeader = serializer.deserialize(respHead, ResponseMessage.class);

        // Assert that the received response correlates to the request
        assertEquals(mockResponseBody, multiPartResp.get("payload"));
        assertEquals(msgHeader.getId(), responseHeader.getCorrelationMessage());
    }

}
