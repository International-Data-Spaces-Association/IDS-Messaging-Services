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

import java.security.Key;
import java.util.Arrays;
import java.util.Map;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The DapsValidator checks the DAPS Token of a RequestMessage using a public signingKey.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DapsValidator {

    /**
     * The DapsPublicKeyProvider.
     */
    private final DapsPublicKeyProvider keyProvider;

    /**
     * Includes base certificates.
     */
    private final String[] baseSecProfVals =
            {"idsc:BASE_CONNECTOR_SECURITY_PROFILE",
            "idsc:BASE_SECURITY_PROFILE"};

    /**
     * Includes all certificates up to trust.
     */
    private final String[] trustSecProfVals =
            {"idsc:BASE_CONNECTOR_SECURITY_PROFILE",
            "idsc:BASE_SECURITY_PROFILE",
            "idsc:TRUST_SECURITY_PROFILE",
            "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE"};

    /**
     * Includes all certificates up to trust plus.
     */
    private final String[] plusTrustSecProfVals =
            {"idsc:BASE_CONNECTOR_SECURITY_PROFILE",
            "idsc:BASE_SECURITY_PROFILE",
            "idsc:TRUST_SECURITY_PROFILE",
            "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE",
            "idsc:TRUST_PLUS_SECURITY_PROFILE",
            "idsc:TRUSTED_CONNECTOR_PLUS_SECURITY_PROFILE"};

    /**
     * Extract the Claims from the Dat token of a message, given the Message and a publicKey.
     *
     * @param token {@link DynamicAttributeToken} of an incoming RequestMessage.
     * @param publicKey The public Key.
     * @return The Claims of the messages DAT Token, when it can be signed with the given key.
     * @throws ClaimsException If Token cannot be signed with the given key.
     */
    public Jws<Claims> getClaims(@NonNull final DynamicAttributeToken token,
                                 @NonNull final Key publicKey)
            throws ClaimsException {
        final var tokenValue = token.getTokenValue();

        try {
            return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(tokenValue);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "Claims could not be read from the token! [code=(IMSCOW0149),"
                        + " message=({})]", e.getMessage());
            }

            throw new ClaimsException("Claims could not be read from the token! [message=({})]");
        }
    }

    /**
     * Get the claims of the DAT and validate it using the public key of the issuer.
     *
     * @param token Incoming DAT token.
     * @return Claims extracted from the DAT.
     * @throws ClaimsException If token cannot be parsed using a DAPS public key.
     */
    public Jws<Claims> getClaims(final DynamicAttributeToken token) throws ClaimsException {
        //remove signature from token, since public key not yet available
        //(used JWT-Parser requires public key otherwise)
        final var noSigJwt = token.getTokenValue().substring(0,
                    token.getTokenValue().lastIndexOf('.') + 1);

        //read contents of token without signature using the JWT-Parser
        final var claim = Jwts.parser().parseClaimsJwt(noSigJwt);

        //read kid and issuer of token content and request public key from issuer using the kid
        final var key = keyProvider.requestPublicKey(
                claim.getBody().getIssuer(),
                claim.getHeader().get("kid").toString());

        //get claims from token using token and requested public key of issuer
        //(and validate the token using the public key as signingKey in parser)
        return getClaims(token, key);
    }

    /**
     * Check the claims of the DAT.
     *
     * @param claims JWS claims of DAT Token.
     * @param extraAttributes Extra attributes to be checked.
     * @return True, if claims are valid.
     */
    public boolean checkClaims(final Jws<Claims> claims,
                               final Map<String, Object> extraAttributes) {
        if (extraAttributes != null && extraAttributes.containsKey("securityProfile")) {
            try {
                verifySecurityProfile(claims.getBody().get("securityProfile", String.class),
                        extraAttributes.get("securityProfile").toString());
            } catch (ClaimsException e) {
                if (log.isWarnEnabled()) {
                    log.warn("SecurityProfile could not be successfully verified!"
                             + " [message=({}), code=(IMSCOW0033)]", e.getMessage());
                }
                return false;
            }
        }
        try {
            return DapsVerifier.verify(claims);
        } catch (ClaimsException e) {
            if (log.isWarnEnabled()) {
                log.warn("Claims could not be successfully verified! [message=({}),"
                         + " code=(IMSCOW0034)]", e.getMessage());
            }
            return false;
        }
    }

    /**
     * Check a given DAT considering additional attributes from the message payload.
     *
     * @param token {@link DynamicAttributeToken} of an incoming Message.
     * @param extraAttributes Additional Attributes from the Message Payload.
     * @return True if DAT is valid.
     */
    public boolean checkDat(final DynamicAttributeToken token,
                            final Map<String, Object> extraAttributes) {
        Jws<Claims> claims;
        try {
            claims = getClaims(token);
        } catch (ClaimsException | ExpiredJwtException e) {
            return false;
        }
        if (claims == null) {
            return false;
        }

        return checkClaims(claims, extraAttributes);
    }

    /**
     * Check a given DAT.
     *
     * @param token A {@link DynamicAttributeToken} from a Infomodel Message.
     * @return True if DAT is valid.
     */
    public boolean checkDat(final DynamicAttributeToken token) {
        return checkDat(token, null);
    }

    /**
     * Verifies that the {@link de.fraunhofer.iais.eis.SecurityProfile}
     * in the token is  same as in the Selfdescription.
     *
     * @param registered The SecurityProfile ID  in token.
     * @param given The SecurityProfile ID in Selfdescription.
     * @throws ClaimsException If SecurityProfiles do not match.
     */
    private void verifySecurityProfile(final String registered,
                                       final String given)
            throws ClaimsException {
        if (registered == null) {
            throw new ClaimsException("Security profile violation."
                                      + " No security profile given in DAT!");
        }

        final var adjustedRegistered = registered.replace("https://w3id.org/idsa/code/", "idsc:");
        final var adjustedGiven = given.replace("https://w3id.org/idsa/code/", "idsc:");

        String[] includedProfiles;
        switch (adjustedRegistered) {
            case "idsc:BASE_CONNECTOR_SECURITY_PROFILE":
            case "idsc:BASE_SECURITY_PROFILE":
                includedProfiles = baseSecProfVals;
                break;
            case "idsc:TRUST_SECURITY_PROFILE":
            case "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE":
                includedProfiles = trustSecProfVals;
                break;
            case "idsc:TRUST_PLUS_SECURITY_PROFILE":
            case "idsc:TRUSTED_CONNECTOR_PLUS_SECURITY_PROFILE":
                includedProfiles = plusTrustSecProfVals;
                break;
            default:
                includedProfiles = new String[0];
        }

        if (!Arrays.asList(includedProfiles).contains(adjustedGiven)) {
            throw new ClaimsException(
                    "Security profile violation. Registered security profile"
                    + " at DAPS is: " + registered
                    + ", but the given security profile is: " + given);
        }
    }
}
