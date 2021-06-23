package de.fraunhofer.ids.messaging.paris;

import javax.xml.datatype.DatatypeFactory;
import java.net.URI;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.messaging.core.daps.DapsPublicKeyProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsValidator;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.IdsHttpService;
import de.fraunhofer.ids.messaging.protocol.multipart.MessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.DescriptionResponseMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.GenericMessageAndPayload;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.MessageProcessedNotificationMAP;
import de.fraunhofer.ids.messaging.protocol.multipart.mapping.ParticipantNotificationMAP;
import de.fraunhofer.ids.messaging.util.IdsMessageUtils;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith( SpringExtension.class)
@ContextConfiguration(classes = { ParisServiceTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
class ParisServiceTest {

    @Autowired
    private Connector connector;

    @Autowired
    private ConfigurationModel configurationModel;

    @Autowired
    private ConfigContainer configurationContainer;

    @Autowired
    private DapsTokenProvider dapsTokenProvider;

    @Autowired
    private DapsPublicKeyProvider dapsPublicKeyProvider;

    @Autowired
    private DapsValidator dapsValidator;

    @Autowired
    private MessageService messageService;

    private MockWebServer mockWebServer;

    @Autowired
    private ParisService parisService;

    private DynamicAttributeToken fakeToken;

    private MessageProcessedNotificationMessage notificationMessage;

    private DescriptionResponseMessage resultMessage;
    private Participant participant;


    @Configuration
    static class TestContextConfiguration {
        @MockBean
        private KeyStoreManager keyStoreManager;

        @MockBean
        private ConfigurationModel configurationModel;

        @MockBean
        private ConfigContainer configurationContainer;

        @MockBean
        private DapsTokenProvider dapsTokenProvider;

        @MockBean
        private DapsPublicKeyProvider dapsPublicKeyProvider;

        @MockBean
        private DapsValidator dapsValidator;

        @MockBean
        private IdsHttpService idsHttpService;

        @MockBean
        private MessageService messageService;

        @MockBean
        private Connector connector;

        @Bean
        public Serializer getSerializer() {
            return new Serializer();
        }

        @Bean
        public ParisService getBrokerService() {
            return new ParisService(configurationContainer, dapsTokenProvider, messageService);
        }
    }


    @BeforeEach
    public void setUp() throws Exception{
        this.fakeToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_("fake Token")
                .build();
        final var endpoint = new ConnectorEndpointBuilder()
                ._accessURL_(URI.create("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5"))
                .build();
        connector = new BaseConnectorBuilder()
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._outboundModelVersion_("4.0.0")
                ._inboundModelVersion_(Util.asList("4.0.0"))
                ._curator_(URI.create("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5"))
                ._maintainer_(URI.create("https://isst.fraunhofer.de/ids/dc967f79-643d-4780-9e8e-3ca4a75ba6a5"))
                ._hasDefaultEndpoint_(endpoint)
                .build();
        Mockito.when(configurationContainer.getConnector()).thenReturn(connector);
        Mockito.when(configurationContainer.getConfigurationModel()).thenReturn(configurationModel);
        Mockito.when(configurationModel.getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
        Mockito.when(dapsTokenProvider.provideDapsToken()).thenReturn("Mocked Token.");
        Mockito.when(dapsPublicKeyProvider.providePublicKeys()).thenReturn(null);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsValidator.checkDat(fakeToken)).thenReturn(true);
        Mockito.when(dapsTokenProvider.getDAT()).thenReturn(fakeToken);
        this.notificationMessage = new MessageProcessedNotificationMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                ._senderAgent_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                ._securityToken_(this.fakeToken)
                ._modelVersion_("4.0.0")
                ._correlationMessage_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                .build();
        this.resultMessage = new DescriptionResponseMessageBuilder()
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                ._senderAgent_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                ._securityToken_(this.fakeToken)
                ._modelVersion_("4.0.0")
                ._correlationMessage_(
                        URI.create("https://w3id.org/idsa/autogen/baseConnector/691b3a17-0e91-4a5a-9d9a-5627772222e9"))
                .build();
        this.participant = new ParticipantBuilder()
                ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
                ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
                ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
                ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
                ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
                ._memberParticipant_(Util.asList(new ParticipantBuilder(new URI("https://www.fraunhofer.de/"))._legalForm_("e.V.").build()))
                ._participantCertification_(new ParticipantCertificationBuilder()._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)._lastValidDate_(
                        DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0)).build())
                ._memberPerson_(Util.asList(new PersonBuilder()
                                                    ._familyName_("Mueller")
                                                    ._givenName_("Peter")
                                                    ._emailAddress_(Util.asList("mueller.peter@emailcontact.de"))
                                                    ._homepage_("https://MemberPerson.de/mueller_peter")
                                                    ._phoneNumber_(Util.asList("017894561237"))
                                                    .build()))
                ._version_("3.0")
                ._legalForm_("e.V.")
                .build();
    }

    @Test
    void testUpdateParticipantAtParIS() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(notificationMessage);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);


        final var result = this.parisService.updateParticipantAtParIS(URI.create("/"), participant);
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.getClass(), "Method should return MessageProcessedNotificationMessage");
    }


    @Test
    void testUnregisterAtParIS() throws Exception{
        final MessageAndPayload map = new DescriptionResponseMAP(resultMessage, participant.toRdf());
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);
        final var result = this.parisService.requestParticipant(URI.create("/"), URI.create("/"));
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(ParticipantNotificationMAP.class, result.getClass(), "Method should return ParticipantNotificationMAP");
        assertEquals(ParticipantImpl.class, result.getPayload().get().getClass(), "Payload should be Participant");
    }

    @Test
    void testRequestParticipant() throws Exception{
        final MessageAndPayload map = new MessageProcessedNotificationMAP(notificationMessage);
        Mockito.when(messageService.sendIdsMessage(any(GenericMessageAndPayload.class), any(URI.class)))
               .thenReturn(map);


        final var result = this.parisService.unregisterAtParIS(URI.create("/"), participant.getId());
        assertNotNull(result.getMessage(), "Method should return a message");
        assertEquals(MessageProcessedNotificationMAP.class, result.getClass(), "Method should return MessageProcessedNotificationMessage");
    }

}