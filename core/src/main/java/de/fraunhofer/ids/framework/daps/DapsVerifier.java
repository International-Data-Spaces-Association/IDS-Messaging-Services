package de.fraunhofer.ids.framework.daps;

import java.time.Instant;
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
            var valid = body.getNotBefore().before(Date.from(Instant.now()));
            valid &= body.getExpiration().after(Date.from(Instant.now()));
            return valid;
        } catch( Exception e ) {
            log.warn("Could not verify Claims of the DAT Token!");
            throw new ClaimsException(e.getMessage());
        }
    }
}
