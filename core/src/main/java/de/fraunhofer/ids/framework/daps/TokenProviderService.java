package de.fraunhofer.ids.framework.daps;

import java.io.IOException;
import java.security.Key;
import java.util.Objects;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import okhttp3.Request;
import okhttp3.Response;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Get Daps Tokens and Daps Public Key from specified URLs.
 * Spring Component Wrapper for TokenManagerService
 */
@Service
public class TokenProviderService implements DapsTokenProvider, DapsPublicKeyProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenProviderService.class);

    private ConfigContainer configContainer;
    private ClientProvider  clientProvider;
    private Key                    publicKey;

    @Value( "${daps.key.url}" )
    private String dapsKeyUrl;

    @Value( "${daps.token.url}" )
    private String dapsUrl;

    @Value( "${daps.kid.url:default}" )
    private String keyKid;

    /**
     * @param configContainer the {@link ConfigContainer} managing the connector configuration
     * @param clientProvider         the {@link ClientProvider} providing HttpClients using the current connector configuration
     */
    @Autowired
    public TokenProviderService( ConfigContainer configContainer, ClientProvider clientProvider ) {
        this.configContainer = configContainer;
        this.clientProvider = clientProvider;
    }

    /**
     * Return the DAT as a Infomodel {@link DynamicAttributeToken}
     *
     * @return acquire a new DAPS Token and return it as a {@link DynamicAttributeToken}
     */
    @Override
    public DynamicAttributeToken getDAT() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        return new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(provideDapsToken())
                .build();
    }

    /**
     * Return the DAT as JWT String
     *
     * @return acquire a new DAPS Token and return the JWT String value
     */
    @Override
    public String provideDapsToken() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        LOGGER.debug(String.format("Get a new DAT Token from %s", dapsUrl));
        return TokenManagerService.acquireToken(configContainer, clientProvider, dapsUrl);
    }

    /**
     * Return the Public Key from the DAPS JWKS
     *
     * @return the Public Key from the DAPS (used for validating Tokens of incoming Messages)
     */
    @Override
    public Key providePublicKey() {
        if( publicKey == null ) {
            LOGGER.debug(String.format("Getting public key from %s!", dapsKeyUrl));
            getPublicKey();
        }
        LOGGER.debug("Provide public key!");
        return publicKey;
    }

    /**
     * Pull the Public Key from the DAPS and save it in the publicKey variable
     */
    private void getPublicKey() {
        try {
            //request the JWK-Set
            LOGGER.debug(String.format("Getting json web keyset from %s", dapsKeyUrl));
            var client = clientProvider.getClient();
            Request request = new Request.Builder()
                    .url(dapsKeyUrl)
                    .build();
            Response response = client.newCall(request).execute();

            var keySetJSON = Objects.requireNonNull(response.body()).string();
            var jsonWebKeySet = new JsonWebKeySet(keySetJSON);
            var jsonWebKey = jsonWebKeySet.getJsonWebKeys().stream().filter(k -> k.getKeyId().equals(keyKid)).findAny()
                                          .orElse(null);

            if( jsonWebKey != null ) {
                this.publicKey = jsonWebKey.getKey();
            } else {
                LOGGER.warn(
                        "Could not get JsonWebKey with kid " + keyKid + " from received KeySet! PublicKey is null!");
            }
        } catch( IOException e ) {
            LOGGER.warn("Could not get key from " + dapsKeyUrl + "!");
            LOGGER.warn(e.getMessage(), e);
        } catch( JoseException e ) {
            LOGGER.warn("Could not create JsonWebKeySet from response!");
            LOGGER.warn(e.getMessage(), e);
        }
    }
}