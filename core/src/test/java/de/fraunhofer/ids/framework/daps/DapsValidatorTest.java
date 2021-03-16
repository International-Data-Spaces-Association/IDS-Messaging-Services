package de.fraunhofer.ids.framework.daps;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.ResponseMessageBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
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

import java.security.KeyPairGenerator;
import java.sql.Date;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DapsValidatorTest.TestContextConfiguration.class})
@AutoConfigureMockMvc
class DapsValidatorTest {

    @Configuration
    static class TestContextConfiguration{

        @MockBean
        DapsPublicKeyProvider dapsPublicKeyProvider;

        @Bean
        public DapsValidator getDapsValidator(){
            return new DapsValidator(dapsPublicKeyProvider);
        }

    }

    @Autowired
    DapsValidator dapsValidator;

    @Autowired
    DapsPublicKeyProvider dapsPublicKeyProvider;

    @Test
    public void testTokenValidation() throws Exception {
        //DapsValidator should exist
        assertNotNull(dapsValidator);
        //Create an RSA KeyPair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        var pair = keyGen.generateKeyPair();
        //when PublicKeyProvider is called, return generated Public Key
        Mockito.when(dapsPublicKeyProvider.providePublicKey()).thenReturn(pair.getPublic());
        //create JWT and sign it with generated private Key
        var jwt = Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setNotBefore(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(SignatureAlgorithm.RS256, pair.getPrivate());
        //create an Infomodel message with the generated jwt to pass to the dapsValidator
        var messageWithToken = new ResponseMessageBuilder()
                ._securityToken_(
                        new DynamicAttributeTokenBuilder()
                                ._tokenFormat_(TokenFormat.JWT)
                                ._tokenValue_(jwt.compact())
                                .build()
                )
                .build();
        //token should be accepted
        assertTrue(dapsValidator.checkDat(messageWithToken));

        //create a new token, already expired on creation
        jwt = Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setNotBefore(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().minusSeconds(600)))
                .signWith(SignatureAlgorithm.RS256, pair.getPrivate());
        //create an infomodel message with the generated jwt to pass to the dapsValidator
        messageWithToken = new ResponseMessageBuilder()
                ._securityToken_(
                        new DynamicAttributeTokenBuilder()
                                ._tokenFormat_(TokenFormat.JWT)
                                ._tokenValue_(jwt.compact())
                                .build()
                )
                .build();
        //token should be rejected
        assertFalse(dapsValidator.checkDat(messageWithToken));

        //create an infomodel message with some random String as token
        messageWithToken = new ResponseMessageBuilder()
                ._securityToken_(
                        new DynamicAttributeTokenBuilder()
                                ._tokenFormat_(TokenFormat.JWT)
                                ._tokenValue_(RandomStringUtils.randomAlphabetic(12))
                                .build()
                )
                .build();
        //token should be rejected
        assertFalse(dapsValidator.checkDat(messageWithToken));
    }

}