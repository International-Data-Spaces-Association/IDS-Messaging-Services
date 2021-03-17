package de.fraunhofer.ids.framework.daps;

import java.security.Key;
import java.util.Arrays;
import java.util.Map;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The DapsValidator checks the DAPS Token of a RequestMessage using a public signingKey
 */
@Slf4j
@Service
public class DapsValidator {
    private final DapsPublicKeyProvider keyProvider;

    public DapsValidator( final DapsPublicKeyProvider keyProvider ) {
        this.keyProvider = keyProvider;
    }

    private final String[] baseSecProfVals      =
            { "idsc:BASE_CONNECTOR_SECURITY_PROFILE", "idsc:BASE_SECURITY_PROFILE" };
    private final String[] trustSecProfVals     =
            { "idsc:BASE_CONNECTOR_SECURITY_PROFILE", "idsc:BASE_SECURITY_PROFILE", "idsc:TRUST_SECURITY_PROFILE",
                    "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE" };
    private final String[] plusTrustSecProfVals =
            { "idsc:BASE_CONNECTOR_SECURITY_PROFILE", "idsc:BASE_SECURITY_PROFILE", "idsc:TRUST_SECURITY_PROFILE",
                    "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE", "idsc:TRUST_PLUS_SECURITY_PROFILE",
                    "idsc:TRUSTED_CONNECTOR_PLUS_SECURITY_PROFILE" };

    /**
     * Extract the Claims from the Dat token of a message, given the Message and a signingKey
     *
     * @param token      {@link DynamicAttributeToken} of an incoming RequestMessage
     * @param signingKey a public Key
     *
     * @return the Claims of the messages DAT Token, when it can be signed with the given key
     *
     * @throws ClaimsException if Token cannot be signed with the given key
     */
    public static Jws<Claims> getClaims( final DynamicAttributeToken token, final Key signingKey )
            throws ClaimsException {
        var tokenValue = token.getTokenValue();
        try {
            return Jwts.parser()
                       .setSigningKey(signingKey)
                       .parseClaimsJws(tokenValue);
        } catch( Exception e ) {
            log.warn("Could not parse incoming JWT/DAT!");
            throw new ClaimsException(e.getMessage());//NOPMD
        }
    }

    /**
     * Check the DAT of a Message
     *
     * @param token a {@link DynamicAttributeToken} from a Infomodel Message
     *
     * @return true if DAT of Message is valid
     */
    public boolean checkDat( final DynamicAttributeToken token, Map<String, Object> extraAttributes ) {
        Jws<Claims> claims;
        try {
            claims = getClaims(token, keyProvider.providePublicKey());
        } catch( ClaimsException e ) {
            log.warn("Daps token of response could not be parsed!");
            return false;
        }
        if( extraAttributes != null && extraAttributes.containsKey("securityProfile") ) {
            try {
                verifySecurityProfile(claims.getBody().get("securityProfile", String.class),
                                      extraAttributes.get("securityProfile").toString());
            } catch( ClaimsException e ) {
                log.warn("Security profile does not match Selfdescription");
                return false;
            }
        }
        try {
            return DapsVerifier.verify(claims);
        } catch( ClaimsException e ) {
            log.warn("Claims could not be verified!");
            return false;
        }
    }

    /**
     * Vertifies that the {@link de.fraunhofer.iais.eis.SecurityProfile} in the token is  same as in the Selfdescription
     *
     * @param registered the SecurityProfile ID  in token
     * @param given      the SecurityProfile ID in Selfdescription
     *
     * @throws ClaimsException if SecurityProfiles do not match
     */
    private void verifySecurityProfile( String registered, String given ) throws ClaimsException {

        //Replace full URIs (if present) by prefixed values. This simplifies the potential number of values these strings can have
        String adjustedRegistered = registered;
        String adjustedGiven = given;
        if( registered.startsWith("https://w3id.org/idsa/code/") ) {
            adjustedRegistered = registered.replace("https://w3id.org/idsa/code/", "idsc:");
        }
        if( given.startsWith("https://w3id.org/idsa/code/") ) {
            adjustedGiven = given.replace("https://w3id.org/idsa/code/", "idsc:");
        }
        String[] includedProfiles;
        switch( adjustedRegistered ) {
            case "idsc:BASE_CONNECTOR_SECURITY_PROFILE":
            case "idsc:BASE_SECURITY_PROFILE":
                includedProfiles = baseSecProfVals;
                break;
            case "idsc:TRUST_SECURITY_PROFILE:":
            case "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE:":
                includedProfiles = trustSecProfVals;
                break;
            case "idsc:TRUST_PLUS_SECURITY_PROFILE:":
            case "idsc:TRUSTED_CONNECTOR_PLUS_SECURITY_PROFILE:":
                includedProfiles = plusTrustSecProfVals;
                break;
            default:
                includedProfiles = new String[0];
        }
        if( !Arrays.asList(includedProfiles).contains(adjustedGiven) ) {
            throw new ClaimsException(
                    "Security profile violation. Registered security profile at DAPS is:  " + registered
                    + ", but the given security profile is: " + given);
        }
    }

}
