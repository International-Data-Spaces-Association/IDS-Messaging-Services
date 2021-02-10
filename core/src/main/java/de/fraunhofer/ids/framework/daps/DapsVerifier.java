package de.fraunhofer.ids.framework.daps;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DefaultVerifier contains some default DAPS verification rules.
 */
public class DapsVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DapsVerifier.class);

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
    public static boolean verify( Jws<Claims> toVerify ) throws ClaimsException {
        try {
            Claims body = toVerify.getBody();
            var valid = body.getNotBefore().before(Date.from(Instant.now()));
            valid &= body.getExpiration().after(Date.from(Instant.now()));
            //TODO more DAT checks
            return valid;
        }catch (Exception e){
            LOGGER.warn("Could not verify Claims of the DAT Token!");
            throw new ClaimsException(e.getMessage());
        }
    }
}