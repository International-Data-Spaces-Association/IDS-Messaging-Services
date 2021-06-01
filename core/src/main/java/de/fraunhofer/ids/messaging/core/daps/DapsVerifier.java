package de.fraunhofer.ids.messaging.core.daps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.fraunhofer.ids.messaging.core.daps.customcheck.CustomDapsRule;
import de.fraunhofer.ids.messaging.core.daps.customcheck.CustomDapsRuleException;
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
     * Custom DAT validation rules, which are checked additionally to the default checks
     */
    private static List<CustomDapsRule> customDapsRules = new ArrayList<>();

    /**
     * Add a custom validation rule to check the DAT
     *
     * @param customDapsRule {@link CustomDapsRule} to add
     */
    public void registerCustomDapsRule(CustomDapsRule customDapsRule){
        customDapsRules.add(customDapsRule);
    }


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
            if (log.isDebugEnabled()) {
                log.debug("Checking custom rules...");
            }
            //check custom dat rules
            if(customDapsRules != null && !customDapsRules.isEmpty()){
                for(var rule : customDapsRules){
                    try {
                        var result = rule.checkRule(toVerify);
                        if(!result.isSuccess()){
                            if (log.isWarnEnabled()) {
                                log.warn("Custom DAT validation rule failed!");
                            }
                            throw new ClaimsException(String.format("Custom Rule failed! Message: %s", result.getMessage()));
                        }
                    } catch (CustomDapsRuleException e) {
                        if (log.isWarnEnabled()) {
                            log.warn("Exception thrown by custom DAT validation rule!");
                        }
                        throw new ClaimsException(String.format("Custom Rule threw Exception! Message: %s", e.getMessage()));
                    }
                }
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
