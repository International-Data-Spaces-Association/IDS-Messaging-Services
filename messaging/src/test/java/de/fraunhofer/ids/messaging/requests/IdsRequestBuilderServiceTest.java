package de.fraunhofer.ids.messaging.requests;

import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.exceptions.IdsRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class IdsRequestBuilderServiceTest {

    @Autowired
    private IdsRequestBuilderService idsRequestBuilderService;

    @Autowired
    private RequestTemplateProvider requestTemplateProvider;

    @Test
    public void test() throws DapsTokenManagerException, IdsRequestException, ClaimsException, MultipartParseException, IOException {
        MessageContainer<Object> response = idsRequestBuilderService
                                                    .newRequest() //create a new RequestBuilder instance
                                                    .useTemplate(requestTemplateProvider.artifactRequestMessageMessageTemplate(URI.create("http://artifact"))) //mandatory: get a template (or create own Template using Lambda: () -> RequestMessage). Calling execute without set template leads to NoTemplateProvidedException.
                                                    .throwOnRejection() //optional: RejectionMessages will not be put into a MessageContainer, instead an IdsRequestException containing the RejectionReason will be thrown.
                                                    .execute(URI.create("http://target")); //send message to target uri and put response into MessageContainer

        MessageContainer<Artifact> resp = idsRequestBuilderService.newRequestExpectingType(Artifact.class)
                                                    .useTemplate(requestTemplateProvider.artifactRequestMessageMessageTemplate(URI.create("http://artifact"))) //mandatory: get a template (or create own Template using Lambda: () -> RequestMessage). Calling execute without set template leads to NoTemplateProvidedException.
                                                    .throwOnRejection() //optional: RejectionMessages will not be put into a MessageContainer, instead an IdsRequestException containing the RejectionReason will be thrown.
                                                    .execute(URI.create("http://target")); //send message to target uri and put response into MessageContainer

    }
}