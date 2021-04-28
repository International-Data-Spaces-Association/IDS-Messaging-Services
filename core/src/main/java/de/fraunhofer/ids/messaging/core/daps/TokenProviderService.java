package de.fraunhofer.ids.messaging.core.daps;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.ids.messaging.core.config.ClientProvider;
import io.jsonwebtoken.Claims;
import lombok.AccessLevel;
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
    List<Key>   publicKeys;

    @Value("${daps.token.url}")
    String dapsTokenUrl;

    @Value("#{${daps.key.url.kid}}")
    Map<String, String> urlKidMap;

    /**
     * @param clientProvider      the {@link ClientProvider} providing HttpClients using the current connector configuration
     * @param tokenManagerService client to get a DAT Token from a DAPS (eg Orbiter/AISEC)
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
    public List<Key> providePublicKeys() {
        if (publicKeys == null) {
            getPublicKeys();
        }

        if (log.isDebugEnabled()) {
            log.debug("Provide public key!");
        }

        return publicKeys;
    }

    /**
     * Pull the Public Key from the DAPS and save it in the publicKey variable.
     */
    @PostConstruct
    private void getPublicKeys() {
        this.publicKeys = new ArrayList<>();
        //request the JWK-Set
        final var client = clientProvider.getClient();

        for (final var entry : urlKidMap.entrySet()) {
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
                    this.publicKeys.add(jsonWebKey.getKey());
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
        final var token = new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_(jwt).build();

        Claims claims;
        try {
            claims = DapsValidator.getClaims(token, this.publicKeys).getBody();
        } catch (ClaimsException e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not parse jwt!");
            }

            return true;
        }
        return claims.getExpiration().before(Date.from(Instant.now()));
    }
}