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
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import ids.messaging.core.config.ClientProvider;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Get Daps Tokens and Daps Public Key from specified URLs.
 * Spring Component Wrapper for TokenManagerService.
 */
@Slf4j
@Service
public class TokenProviderService implements DapsTokenProvider, DapsPublicKeyProvider {

    /**
     * Negative leeway for expiration of cached DAT.
     */
    private static final int EXPIRATION_LEEWAY = 5;

    /**
     * The ClientProvider.
     */
    private final ClientProvider clientProvider;

    /**
     * The TokenManagerService.
     */
    private final TokenManagerService tokenManagerService;

    /**
     * The current jwt.
     */
    private String currentJwt;

    /**
     * The expiration date of the cached DAT.
     */
    private Date expiration;

    /**
     * The DAPS token URL.
     */
    @Value("${daps.token.url}")
    private String dapsTokenUrl;

    /**
     * The well-known path for the public key of the token issuer.
     */
    @Value("${daps.incoming.dat.default.wellknown:}")
    private String dafaultWellKnown;

    /**
     * Used to switch DAT caching on and off.
     */
    @Value("#{new Boolean('${daps.enable.cache.dat:true}')}")
    private Boolean cacheDat;

    /**
     * Constructor for TokenProviderService.
     *
     * @param clientProvider The ClientProvider.
     * @param tokenManagerService The TokenManagerService.
     */
    @Autowired
    public TokenProviderService(final ClientProvider clientProvider,
                                final TokenManagerService tokenManagerService) {
        this.clientProvider = clientProvider;
        this.tokenManagerService = tokenManagerService;
    }

    /**
     * Return the DAT as a Infomodel {@link DynamicAttributeToken}.
     *
     * @return Acquire a new DAPS Token and return it as a {@link DynamicAttributeToken}.
     */
    @Override
    public DynamicAttributeToken getDAT()
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException {
        return new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(provideDapsToken())
                .build();
    }

    /**
     * Return the DAT as JWT String.
     *
     * @return Acquire a new DAPS Token and return the JWT String value.
     */
    @Override
    public String provideDapsToken()
            throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException {
        if (!cacheDat || currentJwt == null || isExpired()) {
            if (log.isDebugEnabled()) {
                log.debug("Requesting a new DAT Token from DAPS! [code=(IMSCOD0101), url=({})]",
                          dapsTokenUrl);
            }

            currentJwt = tokenManagerService.acquireToken(dapsTokenUrl);
            cacheExpiration();
        }

        return currentJwt;
    }

    /**
     * Will cache the expiration date of the acquired DAPS DAT.
     */
    private void cacheExpiration() {
        try {
            final var token = new DynamicAttributeTokenBuilder()
                    ._tokenFormat_(TokenFormat.JWT)
                    ._tokenValue_(currentJwt)
                    .build();

            //remove signature from own token to read the expiration date
            //(JWT-Parser requires public key of DAPS used otherwiese,
            //saves call to DAPS to obtain public key), only needed for caching expiration date
            final var tokenValue = token.getTokenValue();
            final var noSigJwt = tokenValue.substring(0, tokenValue.lastIndexOf('.') + 1);
            final var claims = Jwts.parser().parseClaimsJwt(noSigJwt);

            expiration = claims.getBody().getExpiration();
        } catch (Exception e) {
            //Will force acquire a new token next message request.
            expiration = null;
        }
    }

    /**
     * Try to get the Public Key with kid from jwks of issuer DAPS (infos from DAT).
     *
     * @param issuer Base uri of DAT issuer DAPS.
     * @param kid kid of public key from jwks (info from incoming DAT).
     * @return publicKey with kid from jwks of issuer DAPS (or null if it does not exist)
     */
    @Override
    public Key requestPublicKey(@NonNull final String issuer, @NonNull final String kid) {
        try {
            var issuerBase = issuer;
            if (issuerBase.endsWith("/")) {
                issuerBase = issuerBase.substring(0, issuer.length() - 1);
            }

            var pubKeysUrl = issuerBase;
            if (dafaultWellKnown == null || dafaultWellKnown.isBlank()) {
                pubKeysUrl += "/.well-known/jwks.json";
            } else {
                pubKeysUrl += dafaultWellKnown;
            }

            if (log.isInfoEnabled()) {
                log.info("Requesting public key of token issuer. "
                         + "[url=({}), kid=({}), code=(IMSCOI0051)]", pubKeysUrl, kid);
            }

            final var client = clientProvider.getClient();
            final var request = new Request.Builder().url(pubKeysUrl).build();
            final var response = client.newCall(request).execute();
            final var keySetJSON = Objects.requireNonNull(response.body()).string();
            final var jsonWebKeySet = new JsonWebKeySet(keySetJSON);

            //Get public key for given kid
            final var jsonWebKey =
                    jsonWebKeySet.getJsonWebKeys().stream().filter(
                            k -> k.getKeyId().equals(kid)
                    ).findAny().orElse(null);

            if (jsonWebKey != null) {
                //if public key was found return it
                return jsonWebKey.getKey();
            } else {
                //if no key was found, return null
                if (log.isWarnEnabled()) {
                    log.warn(String.format(
                            "No key found at DAPS url, kid combination %s:%s!", pubKeysUrl, kid)
                    );
                }
                if (log.isWarnEnabled()) {
                    log.warn("No public key for DAPS was found for DAT to verify claims!"
                             + " [code=(IMSCOW0147), url=[{}], kid=({})]", pubKeysUrl, kid);
                }
                return null;
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Exception while requesting public key from token issuer!"
                          + " [code=(IMSCOE0148), message=[{}]]", e.getMessage());
            }
            return null;
        }
    }

    /**
     * @return True if jwt expired.
     */
    private boolean isExpired() {
        final var expired = expiration == null
                || expiration.before(Date.from(Instant.now().plusSeconds(EXPIRATION_LEEWAY)));

        if (currentJwt != null) {
            //Will only log if DAT was successfully acquired.
            if (expired && log.isInfoEnabled()) {
                log.info("Cached DAPS DAT expired or no expiration set."
                                + " [expiration=({}), code=(IMSCOI0052)]",
                         expiration);
            } else if (log.isInfoEnabled()) {
                log.info("Using cached DAPS DAT. [expiration=({}), code=(IMSCOI0053)]",
                         expiration);
            }
        }

        return expired;
    }
}
