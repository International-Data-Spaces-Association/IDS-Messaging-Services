/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.core.daps;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import ids.messaging.core.daps.customvalidation.ValidationRuleResult;
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
