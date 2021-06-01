package de.fraunhofer.ids.messaging.core.daps.customcheck;

import io.jsonwebtoken.Claims;

@FunctionalInterface
public interface CustomDapsRule {
    CustomRuleResult checkRule(Claims toVerify) throws CustomDapsRuleException;
}
