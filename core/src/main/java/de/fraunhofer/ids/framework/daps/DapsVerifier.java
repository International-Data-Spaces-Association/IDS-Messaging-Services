package de.fraunhofer.ids.framework.daps;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * The DefaultVerifier contains some default DAPS verification rules.
 */
@Slf4j
@UtilityClass
public class DapsVerifier {
    /**
     * Check notbefore and expiration of the DAT Token Claims.
     * The default rules check if the current Time is between NotBefore and Expiration
     *
     * @param toVerify the claims to verify
     * @return true if message is valid
     * @throws ClaimsException when the claims of the DAT cannot be verified
     */
    public boolean verify(final Jws<Claims> toVerify) throws ClaimsException {
        if (toVerify != null) {
            return verify(toVerify.getBody());
        }

        throw new ClaimsException("Could not verify claims, input was null!");
    }

    /**
     * Check notbefore and expiration of the DAT Token Claims.
     * The default rules check if the current Time is between NotBefore and Expiration
     *
     * @param toVerify the claims to verify
     * @return true if message is valid
     * @throws ClaimsException when the claims of the DAT cannot be verified
     */
    public boolean verify(final Claims toVerify) throws ClaimsException {
        try {
            if (toVerify.getExpiration().before(new Date())) {
                throw new ClaimsException("The token is outdated.");
            }

            if (toVerify.getExpiration().before(toVerify.getIssuedAt()) || new Date().before(toVerify.getIssuedAt())) {
                throw new ClaimsException("The token's issued time (iat) is invalid");
            }

            if (new Date().before(toVerify.getNotBefore())) {
                throw new ClaimsException("The token's not before time is invalid");
            }

            if (log.isInfoEnabled()) {
                log.info("Claims verified successfully");
            }

            return true;

        } catch (NullPointerException e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not verify Claims of the DAT Token!");
            }

            throw new ClaimsException(e.getMessage());
        }
    }
}
