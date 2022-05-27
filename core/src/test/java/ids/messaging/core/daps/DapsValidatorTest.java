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

import java.security.KeyPairGenerator;
import java.sql.Date;
import java.time.Instant;
import java.util.Map;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DapsValidatorTest.TestContextConfiguration.class})
class DapsValidatorTest {
    @Autowired
    DapsValidator dapsValidator;

    @Autowired
    DapsPublicKeyProvider dapsPublicKeyProvider;

    @Configuration
    static class TestContextConfiguration{

        @MockBean
        DapsPublicKeyProvider dapsPublicKeyProvider;

        @Bean
        public DapsValidator getDapsValidator(){
            return new DapsValidator(dapsPublicKeyProvider);
        }

    }

    @Test
    void testTokenValidation() throws Exception {
        //DapsValidator should exist
        assertNotNull(dapsValidator);
        //Create an RSA KeyPair
        final var keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        final var pair = keyGen.generateKeyPair();
        //when PublicKeyProvider is called, return generated Public Key
        Mockito.when(dapsPublicKeyProvider.requestPublicKey("test","test"))
               .thenReturn(pair.getPublic());
        //create JWT and sign it with generated private Key
        var jwt = Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setNotBefore(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .setHeaderParam("kid","test")
                .setIssuer("test")
                .signWith(SignatureAlgorithm.RS256, pair.getPrivate());
        var datToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(jwt.compact())
                .build();
        //token should be accepted
        assertTrue(dapsValidator.checkDat(datToken, Map.of()));

        //create a new token, already expired on creation
        jwt = Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setNotBefore(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().minusSeconds(600)))
                .signWith(SignatureAlgorithm.RS256, pair.getPrivate());
        datToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(jwt.compact())
                .build();
        //token should be rejected
        assertFalse(dapsValidator.checkDat(datToken, Map.of()));
    }

    @Test
    void testExtraAttributes() throws Exception {
        assertNotNull(dapsValidator);
        //Create an RSA KeyPair
        final var keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        final var pair = keyGen.generateKeyPair();
        //when PublicKeyProvider is called, return generated Public Key
        Mockito.when(dapsPublicKeyProvider.requestPublicKey("test","test")).thenReturn(pair.getPublic());
        //create JWT with BASE_CONNECTOR_SECURITY_PROFILE
        var jwt = Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setNotBefore(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .setHeaderParam("kid","test")
                .setIssuer("test")
                .claim("securityProfile", "idsc:BASE_CONNECTOR_SECURITY_PROFILE")
                .signWith(SignatureAlgorithm.RS256, pair.getPrivate());
        var datToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(jwt.compact())
                .build();
        //token with correct security profile should be accepted
        assertTrue(dapsValidator.checkDat(datToken, Map.of("securityProfile", "idsc:BASE_CONNECTOR_SECURITY_PROFILE")));
        assertTrue(dapsValidator.checkDat(datToken, Map.of("securityProfile", "idsc:BASE_SECURITY_PROFILE")));
        //other security profiles in extraAttributes should lead to rejection
        assertFalse(dapsValidator.checkDat(datToken, Map.of("securityProfile", "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE")));
        assertFalse(dapsValidator.checkDat(datToken, Map.of("securityProfile", "aiosdhiuaghduiwh")));
        //create JWT with some random securityprofile string
        jwt = Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setNotBefore(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .setHeaderParam("kid","test")
                .setIssuer("test")
                .claim("securityProfile", "sjondfasdhfiou")
                .signWith(SignatureAlgorithm.RS256, pair.getPrivate());
        datToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(jwt.compact())
                .build();
        //token with random string in securityProfile claim should be rejected
        assertFalse(dapsValidator.checkDat(datToken, Map.of("securityProfile", "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE")));
        //create JWT without securityprofile
        jwt = Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setNotBefore(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .setHeaderParam("kid","test")
                .setIssuer("test")
                .signWith(SignatureAlgorithm.RS256, pair.getPrivate());
        datToken = new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(jwt.compact())
                .build();
        //token without security profile should be rejected
        assertFalse(dapsValidator.checkDat(datToken, Map.of("securityProfile", "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE")));
    }

}
