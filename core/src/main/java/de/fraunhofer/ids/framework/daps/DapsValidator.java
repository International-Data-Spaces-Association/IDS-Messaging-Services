package de.fraunhofer.ids.framework.daps;

import java.io.IOException;
import java.security.Key;
import java.util.Map;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.util.MultipartParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The DapsValidator checks the DAPS Token of a RequestMessage using a public signingKey
 */
@Service
public class DapsValidator {

    private DapsPublicKeyProvider keyProvider;
    private Serializer            serializer = new Serializer();

    public DapsValidator( DapsPublicKeyProvider keyProvider ) {
        this.keyProvider = keyProvider;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DapsValidator.class);

    /**
     * Extract the Claims from the Dat token of a message, given the Message and a signingKey
     *
     * @param message    an incoming RequestMessage
     * @param signingKey a public Key
     *
     * @return the Claims of the messages DAT Token, when it can be signed with the given key
     *
     * @throws ClaimsException if Token cannot be signed with the given key
     */
    public static Jws<Claims> getClaims( Message message, Key signingKey ) throws ClaimsException {
        String tokenValue = message.getSecurityToken().getTokenValue();
        try {
            return Jwts.parser()
                       .setSigningKey(signingKey)
                       .parseClaimsJws(tokenValue);
        } catch( Exception e ) {
            LOGGER.warn("Could not parse incoming JWT/DAT!");
            throw new ClaimsException(e.getMessage());
        }
    }

    /**
     * Check the DAT of a Message
     *
     * @param message an Message from a response
     *
     * @return true if DAT of Message is valid
     */
    public boolean checkDat( Message message ) {
        Jws<Claims> claims;
        try {
            claims = getClaims(message, keyProvider.providePublicKey());
        } catch( ClaimsException e ) {
            LOGGER.warn("Daps token of response could not be pased!");
            return false;
        }
        try {
            return DapsVerifier.verify(claims);
        } catch( ClaimsException e ) {
            LOGGER.warn("Claims could not be verified!");
            return false;
        }
    }

    /**
     * Check the DAT of an incoming Response body (as string)
     *
     * @param responseBody string of incoming response body
     *
     * @return true if DAT of response is valid
     */
    public boolean checkDat( String responseBody ) {
        Map<String, String> responseMap;
        Message responseHeader;
        try {
            responseMap = MultipartParser.stringToMultipart(responseBody);
        } catch( FileUploadException e ) {
            LOGGER.warn("Response cannot be parsed to multipart!");
            return false;
        }
        try {
            responseHeader = serializer.deserialize(responseMap.get("header"), Message.class);
        } catch( IOException e ) {
            LOGGER.warn("Response header cannot be deserialized to IDS Message!");
            return false;
        }
        return checkDat(responseHeader);
    }

}
