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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.ids.messaging.core.config.ClientProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Get Daps Tokens and Daps Public Key from specified URLs.
 * Spring Component Wrapper for TokenManagerService
 */
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenProviderService implements DapsTokenProvider, DapsPublicKeyProvider {
    final ClientProvider      clientProvider;
    final TokenManagerService tokenManagerService;

    String      currentJwt;

    @Value("${daps.token.url}")
    String dapsTokenUrl;

    @Value("#{${daps.key.url.kid}}")
    Map<String, String> urlKidMap;

    Map<String, Key> publicKeyMap;

    /**
     * @param clientProvider      the {@link ClientProvider} providing HttpClients using the current connector configuration
     * @param tokenManagerService client to get a DAT Token from a DAPS (eg Orbiter/AISEC)
     */
    @Autowired
    public TokenProviderService(final ClientProvider clientProvider,
                                final TokenManagerService tokenManagerService) {
        this.clientProvider = clientProvider;
        this.tokenManagerService = tokenManagerService;
        this.publicKeyMap = new HashMap<>();
    }

    /**
     * Return the DAT as a Infomodel {@link DynamicAttributeToken}.
     *
     * @return acquire a new DAPS Token and return it as a {@link DynamicAttributeToken}
     */
    @Override
    public DynamicAttributeToken getDAT()
            throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        return new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(provideDapsToken())
                .build();
    }

    /**
     * Return the DAT as JWT String.
     *
     * @return acquire a new DAPS Token and return the JWT String value
     */
    @Override
    public String provideDapsToken()
            throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        if (this.currentJwt == null || isExpired(currentJwt)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Get a new DAT Token from %s", dapsTokenUrl));
            }

            currentJwt = tokenManagerService.acquireToken(dapsTokenUrl);
        }

        return currentJwt;
    }

    /**
     * Return the Public Key from the DAPS JWKS.
     *
     * @return the Public Key from the DAPS (used for validating Tokens of incoming Messages)
     */
    @Override
    public Set<Key> providePublicKeys() {
        //while not all trusted keys are cached, try to get the rest of them
        if (publicKeyMap.size() < urlKidMap.size()) {
            getPublicKeys();
        }

        if (log.isDebugEnabled()) {
            log.debug("Provide public key!");
        }

        return new HashSet<>(publicKeyMap.values());
    }

    /**
     * Try to get the Public Key with kid keyId from jwks of issuer DAPS
     *
     * @param issuer base uri of issuer DAPS
     * @param keyId kid of public key from jwks
     * @return publicKey with kid from jwks of issuer DAPS (or null if it does not exist)
     */
    @Override
    public Key providePublicKey(String issuer, String keyId){
        try{
            final var client = clientProvider.getClient();

            //Standard JWKS location is /.well-known/jwks.json
            final var dapsUrl = issuer + "/.well-known/jwks.json";

            //if public key from this combination is already cached, use this one
            if(publicKeyMap.containsKey(String.format("%s:%s", dapsUrl, keyId))){
                return publicKeyMap.get(String.format("%s:%s", dapsUrl, keyId));
            }

            //only pull public key if it is in set of trusted keys
            if(urlKidMap.getOrDefault(dapsUrl, "").equals(keyId)){
                final var request = new Request.Builder().url(dapsUrl).build();
                final var response = client.newCall(request).execute();
                final var keySetJSON = Objects.requireNonNull(response.body()).string();
                final var jsonWebKeySet = new JsonWebKeySet(keySetJSON);
                final var jsonWebKey =
                        jsonWebKeySet.getJsonWebKeys().stream().filter(k -> k.getKeyId().equals(keyId))
                                .findAny()
                                .orElse(null);
                if(jsonWebKey != null) {
                    //if a public key was found, save it in publicKeyMap and return the key
                    var key = jsonWebKey.getKey();
                    publicKeyMap.put(dapsUrl + ":" + keyId, key);
                    return key;
                }else{
                    //if no key was found, return null
                    if(log.isWarnEnabled()){
                        log.warn(String.format("No key found at DAPS url, kid combination %s:%s!", dapsUrl, keyId));
                    }
                    return null;
                }
            }
            if(log.isWarnEnabled()){
                log.warn(String.format("DAT url, kid combination %s:%s not from a trusted DAPS!", dapsUrl, keyId));
            }
            //if key is not trusted, return null
            return null;
        }catch (IOException | JoseException e){
            if(log.isErrorEnabled()){
                log.error("Exception while pulling public key from DAPS!");
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * Pull the Public Key from the DAPS and save it in the publicKey variable.
     */
    @PostConstruct
    private void getPublicKeys() {
        //request the JWK-Set
        final var client = clientProvider.getClient();

        for (final var entry : urlKidMap.entrySet()) {
            //if key was already saved, skip it
            if(publicKeyMap.containsKey(String.format("%s:%s", entry.getKey(), entry.getValue()))) continue;
            try {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Getting json web keyset from %s", entry.getKey()));
                }

                final var request = new Request.Builder().url(entry.getKey()).build();
                final var response = client.newCall(request).execute();
                final var keySetJSON = Objects.requireNonNull(response.body()).string();
                final var jsonWebKeySet = new JsonWebKeySet(keySetJSON);
                final var jsonWebKey =
                        jsonWebKeySet.getJsonWebKeys().stream().filter(k -> k.getKeyId().equals(entry.getValue()))
                                     .findAny()
                                     .orElse(null);

                if (jsonWebKey != null) {
                    this.publicKeyMap.put(String.format("%s:%s", entry.getKey(), entry.getValue()),jsonWebKey.getKey());
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not get JsonWebKey with kid " + entry.getValue() + " from received KeySet! PublicKey is null!");
                    }
                }
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not get key from " + entry.getKey() + "!");
                    log.warn(e.getMessage(), e);
                }
            } catch (JoseException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not create JsonWebKeySet from response!");
                    log.warn(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * @param jwt the jwt to check expiration
     * @return true if jwt expired
     */
    private boolean isExpired(final String jwt) {
        var noSigJwt = jwt.substring(0, jwt.lastIndexOf('.') + 1);
        var claims = Jwts.parser()
                .parseClaimsJwt(noSigJwt);
        return claims.getBody().getExpiration().before(Date.from(Instant.now()));
    }
}
