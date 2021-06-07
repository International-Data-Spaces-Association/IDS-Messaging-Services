package de.fraunhofer.ids.messaging.core.daps;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import de.fraunhofer.ids.messaging.core.daps.customvalidation.ValidationRuleResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DapsVerifierTest {

    @Test
    void testDapsVerifier() throws Exception {
        //verifying null should throw a ClaimsException
        final Claims nullClaims = null;
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(nullClaims));
        //create valid claims
        final var claims = Jwts.claims();
        claims.setIssuedAt(Date.from(Instant.now()));
        claims.setNotBefore(Date.from(Instant.now()));
        claims.setExpiration(Date.from(Instant.now().plusSeconds(100)));
        claims.setIssuer("Test");
        //verifying valid claims should return true
        assertTrue(DapsVerifier.verify(claims));
        //create expired claims
        final var expiredClaims = Jwts.claims();
        expiredClaims.setIssuedAt(Date.from(Instant.now().minusSeconds(100)));
        expiredClaims.setNotBefore(Date.from(Instant.now().minusSeconds(100)));
        expiredClaims.setExpiration(Date.from(Instant.now()));
        //expired claims should not be accepted
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(expiredClaims));
        //create invalid claims (current time before notbefore)
        final var invalidClaims = Jwts.claims();
        invalidClaims.setIssuedAt(Date.from(Instant.now()));
        invalidClaims.setNotBefore(Date.from(Instant.now().plusSeconds(100)));
        invalidClaims.setExpiration(Date.from(Instant.now().plusSeconds(200)));
        //invalid claims should also be rejected
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(invalidClaims));
        DapsVerifier.addValidationRule(toVerify -> ValidationRuleResult.success());
        assertTrue(DapsVerifier.verify(claims));
        DapsVerifier.addValidationRule(claim -> "Test".equals(claim.getIssuer()) ?
                ValidationRuleResult.success() :
                ValidationRuleResult.failure("This rule sometimes fails!")
        );
        assertTrue(DapsVerifier.verify(claims));
        claims.setIssuer("Test123");
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(claims));
        DapsVerifier.addValidationRule(claim -> ValidationRuleResult.failure("This rule always fails!"));
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(claims));
        final var field = DapsVerifier.class.getDeclaredField("datValidationRules");
        field.setAccessible(true);
        field.set(null, new ArrayList<>());
    }

}
