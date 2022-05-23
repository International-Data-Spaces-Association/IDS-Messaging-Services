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
package ids.messaging.protocol.multipart;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import de.fraunhofer.iais.eis.ConnectorUpdateMessageBuilder;
import de.fraunhofer.iais.eis.ContractAgreementBuilder;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessageBuilder;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.ContractOfferMessage;
import de.fraunhofer.iais.eis.ContractOfferMessageBuilder;
import de.fraunhofer.iais.eis.ContractRejectionMessage;
import de.fraunhofer.iais.eis.ContractRejectionMessageBuilder;
import de.fraunhofer.iais.eis.ContractRequestBuilder;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.ContractRequestMessageBuilder;
import de.fraunhofer.iais.eis.ContractResponseMessage;
import de.fraunhofer.iais.eis.ContractResponseMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.DescriptionResponseMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.NotificationMessage;
import de.fraunhofer.iais.eis.NotificationMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantBuilder;
import de.fraunhofer.iais.eis.ParticipantRequestMessage;
import de.fraunhofer.iais.eis.ParticipantRequestMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantUnavailableMessage;
import de.fraunhofer.iais.eis.ParticipantUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ParticipantUpdateMessage;
import de.fraunhofer.iais.eis.ParticipantUpdateMessageBuilder;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.QueryMessageBuilder;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.ResourceUnavailableMessage;
import de.fraunhofer.iais.eis.ResourceUnavailableMessageBuilder;
import de.fraunhofer.iais.eis.ResourceUpdateMessage;
import de.fraunhofer.iais.eis.ResourceUpdateMessageBuilder;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.DeserializeException;
import ids.messaging.protocol.multipart.mapping.ContractOfferMAP;
import ids.messaging.protocol.multipart.mapping.ContractResponseMAP;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.protocol.multipart.parser.MultipartParser;
import ids.messaging.util.IdsMessageUtils;
import okhttp3.MultipartBody;
import okio.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultipartResponseConverterTest {

    @Test
    void testConvertResponse() throws
            IOException,
            MultipartParseException,
            UnknownResponseException,
            DeserializeException {
        //setup converter
        final var serializer = new Serializer();
        final var converter = new MultipartResponseConverter();

        //check messages with parsed payloads
        //ContractResponseMessage
        final var contractResponseMessage = buildContractResponseMessage();
        final var offer = new ContractOfferBuilder().build();
        var multipartMap = buildMultipart(contractResponseMessage, offer);
        var convertedMAP = converter.convertResponse(multipartMap);
        assertEquals(ContractResponseMAP.class, convertedMAP.getClass());
        assertEquals(contractResponseMessage, convertedMAP.getMessage());
        assertEquals(offer, convertedMAP.getPayload().get());

        //ContractOfferMessage
        final var contractOfferMessage = buildContractOfferMessage();
        multipartMap = buildMultipart(contractOfferMessage, offer);
        convertedMAP = converter.convertResponse(multipartMap);
        assertEquals(ContractOfferMAP.class, convertedMAP.getClass());
        assertEquals(contractOfferMessage, convertedMAP.getMessage());
        assertEquals(offer, convertedMAP.getPayload().get());

        //ContractAgreementMessage
        final var contractAgreementMessage = buildContractAgreementMessage();
        final var agreement = new ContractAgreementBuilder()._contractStart_(IdsMessageUtils.getGregorianNow()).build();
        multipartMap = buildMultipart(contractAgreementMessage, agreement);
        convertedMAP = converter.convertResponse(multipartMap);
        assertEquals(contractAgreementMessage, convertedMAP.getMessage());
        assertEquals(agreement, convertedMAP.getPayload().get());

        //ContractRequestMessage
        final var contractRequestMessage = buildContractRequestMessage();
        final var request = new ContractRequestBuilder().build();
        multipartMap = buildMultipart(contractRequestMessage, request);
        convertedMAP = converter.convertResponse(multipartMap);
        assertEquals(contractRequestMessage, convertedMAP.getMessage());
        assertEquals(request, convertedMAP.getPayload().get());

        //ParticipantUpdateMessage
        final var participantUpdateMessage = buildParticipantUpdateMessage();
        final var participant = new ParticipantBuilder()._legalForm_("form").build();
        multipartMap = buildMultipart(participantUpdateMessage, participant);
        convertedMAP = converter.convertResponse(multipartMap);
        assertEquals(participantUpdateMessage, convertedMAP.getMessage());
        assertEquals(participant, convertedMAP.getPayload().get());

        //ResourceUpdateMessage
        final var resourceUpdateMessage = buildResourceUpdateMessage();
        final var resource = new ResourceBuilder().build();
        multipartMap = buildMultipart(resourceUpdateMessage, resource);
        convertedMAP = converter.convertResponse(multipartMap);
        assertEquals(resourceUpdateMessage, convertedMAP.getMessage());
        assertEquals(resource, convertedMAP.getPayload().get());

        //ConnectorUpdateMessage
        final var connUpdateMessage = buildConnUpdateMessage();
        final var component = new BaseConnectorBuilder()
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._inboundModelVersion_(List.of("1.0.0"))
                ._outboundModelVersion_("1.0.0")
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("http://example.com")).build())
                ._maintainer_(URI.create("http://example.com"))
                ._curator_(URI.create("http://example.com"))
                .build();
        multipartMap = buildMultipart(connUpdateMessage, component);
        convertedMAP = converter.convertResponse(multipartMap);
        assertEquals(connUpdateMessage, convertedMAP.getMessage());
        assertEquals(component, convertedMAP.getPayload().get());

        //messages where payload does not matter for MAP
        final var messages = List.of(
                buildDescriptionRequestMessage(),
                buildDescriptionResponseMessage(),
                buildContractRejectionMessage(),
                buildRejectionMessage(),
                buildArtifactRequestMessage(),
                buildArtifactResponseMessage(),
                buildConnUnavailableMessage(),
                buildQueryMessage(),
                buildResourceUnavailableMessage(),
                buildParticipantUnavailableMessage(),
                buildParticipantRequestMessage()
        );
        for (final var msg : messages){
            final var multiMap = buildMultipart(msg, "payload");
            final var convMap = converter.convertResponse(multiMap);
            assertEquals(msg, convMap.getMessage());
        }

        //check unsupported message (should throw UnknownResponseException)
        final var noteMsg = buildNotificationMessage();
        assertThrows(UnknownResponseException.class, () -> converter.convertResponse(Map.of(
                "header", serializer.serialize(noteMsg),
                "payload", "a")
        ));
    }

    //utility: builder methods

    private Map<String, String> buildMultipart(final Message message, final String payload) throws IOException, MultipartParseException {
        final var serializer = new Serializer();
        final var multipart = new MultipartBody.Builder()
                .addFormDataPart("header", serializer.serialize(message))
                .addFormDataPart("payload", payload)
                .build();
        final Buffer buffer = new Buffer();
        multipart.writeTo(buffer);
        final var multipartString = buffer.readUtf8();
        return MultipartParser.stringToMultipart(multipartString);
    }

    private Map<String, String> buildMultipart(final Message message, final Object payload) throws IOException, MultipartParseException {
        final var serializer = new Serializer();
        return buildMultipart(message, serializer.serialize(payload));
    }

    private NotificationMessage buildNotificationMessage(){
        return new NotificationMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private DescriptionRequestMessage buildDescriptionRequestMessage(){
        return new DescriptionRequestMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private DescriptionResponseMessage buildDescriptionResponseMessage(){
        return new DescriptionResponseMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private RejectionMessage buildRejectionMessage(){
        return new RejectionMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private ContractRejectionMessage buildContractRejectionMessage(){
        return new ContractRejectionMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private ArtifactRequestMessage buildArtifactRequestMessage(){
        return new ArtifactRequestMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                ._requestedArtifact_(URI.create("http://example.com"))
                .build();
    }

    private ArtifactResponseMessage buildArtifactResponseMessage(){
        return new ArtifactResponseMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private ConnectorUpdateMessage buildConnUpdateMessage(){
        return new ConnectorUpdateMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                ._affectedConnector_(URI.create("http://example.com"))
                .build();
    }

    private ConnectorUnavailableMessage buildConnUnavailableMessage(){
        return new ConnectorUnavailableMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                ._affectedConnector_(URI.create("http://example.com"))
                .build();
    }

    private QueryMessage buildQueryMessage(){
        return new QueryMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private ResourceUpdateMessage buildResourceUpdateMessage(){
        return new ResourceUpdateMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                ._affectedResource_(URI.create("http://example.com"))
                .build();
    }

    private ResourceUnavailableMessage buildResourceUnavailableMessage(){
        return new ResourceUnavailableMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                ._affectedResource_(URI.create("http://example.com"))
                .build();
    }

    private ParticipantUpdateMessage buildParticipantUpdateMessage(){
        return new ParticipantUpdateMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                ._affectedParticipant_(URI.create("http://example.com"))
                .build();
    }

    private ParticipantUnavailableMessage buildParticipantUnavailableMessage(){
        return new ParticipantUnavailableMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                ._affectedParticipant_(URI.create("http://example.com"))
                .build();
    }

    private ParticipantRequestMessage buildParticipantRequestMessage(){
        return new ParticipantRequestMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                ._requestedParticipant_(URI.create("http://example.com"))
                .build();
    }

    private ContractOfferMessage buildContractOfferMessage(){
        return new ContractOfferMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private ContractRequestMessage buildContractRequestMessage(){
        return new ContractRequestMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private ContractAgreementMessage buildContractAgreementMessage(){
        return new ContractAgreementMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }

    private ContractResponseMessage buildContractResponseMessage(){
        return new ContractResponseMessageBuilder()
                ._correlationMessage_(URI.create("http://example.com"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("http://example.com"))
                ._modelVersion_("1.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("abc")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(URI.create("http://example.com"))
                .build();
    }
}
