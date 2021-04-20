package de.fraunhofer.ids.framework.daps;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DapsVerifierTest {

    @Test
    public void testDapsVerifier() throws Exception {
        //verifying null should throw a ClaimsException
        Claims nullClaims = null;
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(nullClaims));
        //create valid claims
        var claims = Jwts.claims();
        claims.setIssuedAt(Date.from(Instant.now()));
        claims.setNotBefore(Date.from(Instant.now()));
        claims.setExpiration(Date.from(Instant.now().plusSeconds(100)));
        //verifying valid claims should return true
        assertTrue(DapsVerifier.verify(claims));
        //create expired claims
        var expiredClaims = Jwts.claims();
        expiredClaims.setIssuedAt(Date.from(Instant.now().minusSeconds(100)));
        expiredClaims.setNotBefore(Date.from(Instant.now().minusSeconds(100)));
        expiredClaims.setExpiration(Date.from(Instant.now()));
        //expired claims should not be accepted
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(expiredClaims));
        //create invalid claims (current time before notbefore)
        var invalidClaims = Jwts.claims();
        invalidClaims.setIssuedAt(Date.from(Instant.now()));
        invalidClaims.setNotBefore(Date.from(Instant.now().plusSeconds(100)));
        invalidClaims.setExpiration(Date.from(Instant.now().plusSeconds(200)));
        //invalid claims should also be rejected
        assertThrows(ClaimsException.class, () -> DapsVerifier.verify(invalidClaims));
    }

}