package de.fraunhofer.ids.framework.daps;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;

/**
 * The DefaultVerifier contains some default DAPS verification rules.
 */
@Slf4j
public class DapsVerifier {

    /**
     * Utility classes (only static methods and fields) do not have a public constructor.
     * Instantiating them does not make sense, prevent instantiating.
     */
    protected DapsVerifier() {
        throw new UnsupportedOperationException();
    }

    /**
     * Check notbefore and expiration of the DAT Token Claims
     *
     * The default rules check if the current Time is between NotBefore and Expiration
     *
     * @param toVerify the claims to verify
     *
     * @return true if message is valid
     *
     * @throws ClaimsException when the claims of the DAT cannot be verified
     */
    public static boolean verify( final Jws<Claims> toVerify ) throws ClaimsException {
        try {
            Claims body = toVerify.getBody();
            if( body.getExpiration().before(new Date()) ) {
                throw new ClaimsException("The token is outdated.");
            }
            if( body.getExpiration().before(body.getIssuedAt()) || new Date().before(body.getIssuedAt()) ) {

                throw new ClaimsException("The token's issued time (iat) is invalid");
            }
            if( new Date().before(body.getNotBefore()) ) {
                throw new ClaimsException("The token's not before time is invalid");
            }
            log.info("Claims verified successfully");
            return true;

        } catch( NullPointerException e ) {
            log.warn("Could not verify Claims of the DAT Token!");
            throw new ClaimsException(e.getMessage());
        }
    }
}
