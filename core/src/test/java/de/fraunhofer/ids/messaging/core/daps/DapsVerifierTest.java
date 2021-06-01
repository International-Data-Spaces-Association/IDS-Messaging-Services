package de.fraunhofer.ids.messaging.core.daps;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import de.fraunhofer.ids.messaging.core.daps.customcheck.CustomRuleResult;
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
        DapsVerifier.registerCustomDapsRule(claim -> CustomRuleResult.success());
        assertTrue(DapsVerifier.verify(claims));
        DapsVerifier.registerCustomDapsRule(claim -> claim.getIssuer().equals("Test") ?
                CustomRuleResult.success() :
                CustomRuleResult.failure("This rule sometimes fails!")
        );
        assertTrue(DapsVerifier.verify(claims));
        claims.setIssuer("Test123");
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(claims));
        DapsVerifier.registerCustomDapsRule(claim -> CustomRuleResult.failure("This rule always fails!"));
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(claims));
    }

}
