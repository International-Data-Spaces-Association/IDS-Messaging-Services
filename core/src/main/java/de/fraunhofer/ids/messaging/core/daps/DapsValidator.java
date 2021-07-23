/*
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
 */
package de.fraunhofer.ids.messaging.core.daps;

import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The DapsValidator checks the DAPS Token of a RequestMessage using a public signingKey.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DapsValidator {

    DapsPublicKeyProvider keyProvider;

    String[] baseSecProfVals =
            {"idsc:BASE_CONNECTOR_SECURITY_PROFILE",
            "idsc:BASE_SECURITY_PROFILE"};
    String[] trustSecProfVals =
            {"idsc:BASE_CONNECTOR_SECURITY_PROFILE",
            "idsc:BASE_SECURITY_PROFILE",
            "idsc:TRUST_SECURITY_PROFILE",
            "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE"};
    String[] plusTrustSecProfVals =
            {"idsc:BASE_CONNECTOR_SECURITY_PROFILE",
            "idsc:BASE_SECURITY_PROFILE",
            "idsc:TRUST_SECURITY_PROFILE",
            "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE",
            "idsc:TRUST_PLUS_SECURITY_PROFILE",
            "idsc:TRUSTED_CONNECTOR_PLUS_SECURITY_PROFILE"};

    /**
     * Extract the Claims from the Dat token of a message, given the Message and a signingKey.
     *
     * @param token       {@link DynamicAttributeToken} of an incoming RequestMessage
     * @param signingKeys a  List of public Keys
     * @return the Claims of the messages DAT Token, when it can be signed with the given key
     * @throws ClaimsException if Token cannot be signed with the given key
     */
    public static Jws<Claims> getClaims(final DynamicAttributeToken token,
                                        final List<Key> signingKeys)
            throws ClaimsException {

        final var tokenValue = token.getTokenValue();

        if (signingKeys == null || signingKeys.isEmpty()) {
            throw new ClaimsException("No signing keys were given!");
        }

        for (final var signingKey : signingKeys) {
            try {
                return Jwts.parser()
                           .setSigningKey(signingKey)
                           .parseClaimsJws(tokenValue);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage());
                }
            }
        }

        throw new ClaimsException("No given signing Key could validate JWT!");
    }

    /**
     * @param token incoming DAT token
     * @return claims extracted from the DAT
     * @throws ClaimsException if token cannot be parsed using a DAPS public key
     */
    public Jws<Claims> getClaims(final DynamicAttributeToken token) throws ClaimsException {
        Jws<Claims> claims;
        final var keys = keyProvider.providePublicKeys();

        try {
            claims = getClaims(token, keys);
            return claims;
        } catch (ClaimsException | ExpiredJwtException e) {
            throw e;
        }
    }

    /**
     * Check the claims of the DAT.
     *
     * @param claims JWS claims of DAT Token
     * @param extraAttributes extra attributes to be checked
     * @return true, if claims are valid
     */
    public boolean checkClaims(final Jws<Claims> claims,
                               final Map<String, Object> extraAttributes) {
        if (extraAttributes != null && extraAttributes.containsKey("securityProfile")) {
            try {
                verifySecurityProfile(claims.getBody().get("securityProfile", String.class),
                        extraAttributes.get("securityProfile").toString());
            } catch (ClaimsException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Security profile does not match Selfdescription");
                }
                return false;
            }
        }
        try {
            return DapsVerifier.verify(claims);
        } catch (ClaimsException e) {
            if (log.isWarnEnabled()) {
                log.warn("Claims could not be verified!");
            }
            return false;
        }
    }

    /**
     * Check a given DAT considering additional attributes from the message payload.
     *
     * @param token           {@link DynamicAttributeToken} of an incoming Message
     * @param extraAttributes additional Attributes from the Message Payload
     * @return true if DAT is valid
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
     * @param token a {@link DynamicAttributeToken} from a Infomodel Message
     * @return true if DAT is valid
     */
    public boolean checkDat(final DynamicAttributeToken token) {
        return checkDat(token, null);
    }

    /**
     * Verifies that the {@link de.fraunhofer.iais.eis.SecurityProfile}
     * in the token is  same as in the Selfdescription.
     *
     * @param registered the SecurityProfile ID  in token
     * @param given      the SecurityProfile ID in Selfdescription
     * @throws ClaimsException if SecurityProfiles do not match
     */
    private void verifySecurityProfile(final String registered,
                                       final String given)
            throws ClaimsException {
        //Replace full URIs (if present) by prefixed values.
        //This simplifies the potential number of values these strings can have
        var adjustedRegistered = registered;
        var adjustedGiven = given;

        if (registered == null) {
            throw new ClaimsException("Security profile violation."
                  + " No security profile given in DAT!");
        }

        if (registered.startsWith("https://w3id.org/idsa/code/")) {
            adjustedRegistered = registered.replace("https://w3id.org/idsa/code/", "idsc:");
        }

        if (given.startsWith("https://w3id.org/idsa/code/")) {
            adjustedGiven = given.replace("https://w3id.org/idsa/code/", "idsc:");
        }

        String[] includedProfiles;
        switch (adjustedRegistered) {
            case "idsc:BASE_CONNECTOR_SECURITY_PROFILE":
            case "idsc:BASE_SECURITY_PROFILE":
                includedProfiles = baseSecProfVals;
                break;
            case "idsc:TRUST_SECURITY_PROFILE:":
            case "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE:":
                includedProfiles = trustSecProfVals;
                break;
            case "idsc:TRUST_PLUS_SECURITY_PROFILE:":
            case "idsc:TRUSTED_CONNECTOR_PLUS_SECURITY_PROFILE:":
                includedProfiles = plusTrustSecProfVals;
                break;
            default:
                includedProfiles = new String[0];
        }

        if (!Arrays.asList(includedProfiles).contains(adjustedGiven)) {
            throw new ClaimsException(
                    "Security profile violation. Registered security profile"
                    + " at DAPS is:  " + registered
                    + ", but the given security profile is: " + given);
        }
    }
}
