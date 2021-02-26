package de.fraunhofer.ids.framework.messaging.protocol;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import de.fraunhofer.ids.framework.daps.ClaimsException;
import de.fraunhofer.ids.framework.messaging.protocol.http.IdsHttpService;
import okhttp3.RequestBody;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Option for the connector developer to choose the protocol for sending the message in the IDS dynamically per message.
 * Additionally a default if no protocol is specified.
 */
@Service
public class MessageService {
    private final IdsHttpService httpService;

    /**
     * Constructor of MessageService class
     *
     * @param httpService the IdsHttpService
     */
    @Autowired
    public MessageService( final IdsHttpService httpService ) {
        this.httpService = httpService;
    }

    /**
     * Send messages in IDS to other actors with choice of the protocol used.
     *
     * @param body The RequestBody of the message
     * @param target The target of the message
     * @param protocolType The selected protocol which should be used for sending (see ProtocolType enum)
     * @return returns the response
     * @throws FileUploadException something went wrong with the file attached (if there was one)
     * @throws ClaimsException something went wrong with the DAT
     * @throws IOException DAPS or taget could not be reached
     */
    public Map<String, String> sendIdsMessage( RequestBody body, URI target, ProtocolType protocolType )
            throws FileUploadException, ClaimsException, IOException {
        switch( protocolType ) {
            case MULTIPART:
                return httpService.sendAndCheckDat(body, target);
            case REST:
                return null;
        }
        return null;
    }

    /**
     * Send messages in IDS to other actors without choosing a specific protocol, will use Multipart as default.
     * @param body The RequestBody of the message
     * @param target The target of the message
     * @return returns the response
     * @throws FileUploadException something went wrong with the file attached (if there was one)
     * @throws ClaimsException something went wrong with the DAT
     * @throws IOException DAPS or taget could not be reached
     */
    public Map<String, String> sendIdsMessage( RequestBody body, URI target )
            throws FileUploadException, ClaimsException, IOException {
        return sendIdsMessage(body, target, ProtocolType.MULTIPART);
    }
}
