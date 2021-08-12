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
package de.fraunhofer.ids.messaging.core.daps.aisec;

import java.io.IOException;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.ids.messaging.core.config.ClientProvider;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.messaging.core.config.util.ConnectorUUIDProvider;
import de.fraunhofer.ids.messaging.core.daps.DapsConnectionException;
import de.fraunhofer.ids.messaging.core.daps.DapsEmptyResponseException;
import de.fraunhofer.ids.messaging.core.daps.TokenManagerService;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Manages Dynamic Attribute Tokens.
 *
 * @author Gerd Brost (gerd.brost@aisec.fraunhofer.de)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "daps", name = "mode", havingValue = "aisec")
public class AisecTokenManagerService implements TokenManagerService {
    /**
     * Seconds to add to the expiry date.
     */
    public static final int ONE_DAY_IN_SECONDS  = 86_400;

    /**
     * Seconds to substract for the issued at value.
     */
    public static final int SECONDS_TO_SUBTRACT = 10;

    /**
     * The ClientProvider.
     */
    private final ClientProvider  clientProvider;

    /**
     * The ConfigContainer.
     */
    private final ConfigContainer configContainer;

    /**
     * Get the DAT from the DAPS at dapsURL using the current configuration.
     *
     * @param dapsUrl The URL of a DAPS Service
     * @return signed DAPS JWT token for the Connector
     */
    @Override
    public String acquireToken(final String dapsUrl)
            throws
            DapsConnectionException,
            DapsEmptyResponseException {
        final var keyStoreManager = configContainer.getKeyStoreManager();
        final var targetAudience = "idsc:IDS_CONNECTORS_ALL";

        var dynamicAttributeToken = "INVALID_TOKEN";

        // Try clause for setup phase (loading keys, building trust manager)
        try {
            final var privateKey = getPrivateKey(keyStoreManager);
            final var connectorUUID = ConnectorUUIDProvider.connertorUUID;

            if (log.isInfoEnabled()) {
                log.info("ConnectorUUID: " + connectorUUID);
                log.info("Retrieving Dynamic Attribute Token...");
            }

            final var jws = getRequestToken(targetAudience, privateKey, connectorUUID);

            // build form body to embed client assertion into post request
            final var formBody = getFormBody(jws);

            if (log.isDebugEnabled()) {
                log.debug("Getting client");
            }

            final var client = clientProvider.getClient();
            final var request = new Request.Builder().url(dapsUrl).post(formBody).build();

            if (log.isInfoEnabled()) {
                log.info(String.format("Sending request to DAPS: %s", dapsUrl));
            }

            final var jwtResponse = sendRequestToDAPS(client, request);
            final var responseBody = jwtResponse.body();
            checkEmptyDAPSResponse(responseBody); //can throw exception

            final var jwtString = responseBody.string();

            dynamicAttributeToken = getDAT(jwtString);
        } catch (IOException e) {
            handleIOException(e);
        } catch (DapsEmptyResponseException e) {
            handleDapsEmptyResponseException(e);
        }

        return dynamicAttributeToken;
    }

    /**
     * Handle exception if DAPS returned an empty response.
     *
     * @param e the thrown excpetion
     * @throws DapsEmptyResponseException forwarded exception to the connector
     * developer if DAPS returned an empty response
     */
    private void handleDapsEmptyResponseException(
            final DapsEmptyResponseException e)
            throws DapsEmptyResponseException {
        final var error = String.format("Unusable answer from DAPS: Possible "
            + "empty DAPS-Response, something went wrong at DAPS: %s",
            e.getMessage());

        if (configContainer.getConfigurationModel().getConnectorDeployMode()
           != ConnectorDeployMode.TEST_DEPLOYMENT) {
            printProductiveDeploymentError(error);
            throw new DapsEmptyResponseException(error);
        } else {
            printTestDeploymentWarning(error);
        }
    }

    /**
     * Handle exception if connection to DAPS failed.
     *
     * @param e the thrwon IOException
     * @throws DapsConnectionException mapped exception, thworn if connection to DAPS failed
     */
    private void handleIOException(final IOException e) throws DapsConnectionException {
        final var error = String.format("Error connecting to DAPS "
            + "(not reachable, wrong DAPS-URL or no valid Connector UUID "
            + "(valid connector certificate?)): %s",
            e.getMessage());

        if (configContainer.getConfigurationModel().getConnectorDeployMode()
           != ConnectorDeployMode.TEST_DEPLOYMENT) {
            printProductiveDeploymentError(error);
            throw new DapsConnectionException(error);
        } else {
            printTestDeploymentWarning(error);
        }
    }

    private void printTestDeploymentWarning(final String error) {
        if (log.isWarnEnabled()) {
            log.warn(
                    "TEST_DEPLOYMENT: IDS-Message is sent without a valid DAT, "
                    + "will not be send in PRODUCTIVE_DEPLOYMENT, reason: "
                    + error);
        }
    }

    private void printProductiveDeploymentError(final String error) {
        if (log.isErrorEnabled()) {
            log.error(
                    "PRODUCTIVE_DEPLOYMENT: No IDS-Message sent! "
                    + "No DAT could be loaded from DAPS, reason: "
                    + error);
        }
    }

    /**
     * Get the DAT of the response from DAPS.
     *
     * @param jwtString the DAPS response
     * @return the DAT
     */
    private String getDAT(final String jwtString) {
        final var jsonObject = new JSONObject(jwtString);
        return jsonObject.getString("access_token");
    }

    /**
     * DAPS didn't return a response.
     *
     * @param responseBody the response of the DAPS to be checked
     * @throws DapsEmptyResponseException thrown if response of DAPS is empty
     */
    private void checkEmptyDAPSResponse(final ResponseBody responseBody)
            throws DapsEmptyResponseException {
        if (responseBody == null) {
            throw new DapsEmptyResponseException("JWT response is null.");
        }
    }

    /**
     * Sends the request for a DAT to the DAPS.
     *
     * @param client  the HTTP-Client used for sending messages
     * @param request the request-message to be send
     * @return the response of the DAPS
     * @throws IOException thrown if something went wrong connecting to the DAPS
     */
    @NotNull
    private Response sendRequestToDAPS(final OkHttpClient client,
                                       final Request request)
            throws IOException {
        final var jwtResponse = client.newCall(request).execute();

        if (!jwtResponse.isSuccessful()) {
            if (log.isDebugEnabled()) {
                log.debug("DAPS request was not successful");
            }

            throw new IOException("Unexpected code " + jwtResponse);
        }
        return jwtResponse;
    }

    /**
     * Get the form body for the DAPS-Request.
     *
     * @param jws the generated JWS
     * @return The Request-Formbody
     */
    @NotNull
    private FormBody getFormBody(final String jws) {
        return new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_assertion_type",
                     "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                .add("client_assertion", jws)
                .add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
                .build();
    }

    /**
     * Get the signed(!) request token.
     *
     * @param targetAudience The target audience
     * @param privateKey     the private key of the keystore
     * @param connectorUUID  the UUID of the Connector
     * @return The signed request token
     */
    private String getRequestToken(final String targetAudience,
                                   final PrivateKey privateKey,
                                   final String connectorUUID) {
        if (log.isDebugEnabled()) {
            log.debug("Building jwt token");
        }

        final var expiryDate = Date.from(Instant.now().plusSeconds(ONE_DAY_IN_SECONDS));
        final var jwtb = getJwtBuilder(targetAudience, connectorUUID, expiryDate);

        if (log.isDebugEnabled()) {
            log.debug("Signing jwt token");
        }

        return jwtb.signWith(SignatureAlgorithm.RS256, privateKey).compact();
    }

    /**
     * Get the JWT Builder.
     *
     * @param targetAudience The targetAudience
     * @param connectorUUID  The UUID of the Connector
     * @param expiryDate     The set expiry date
     * @return The JWT-Builder
     */
    private JwtBuilder getJwtBuilder(final String targetAudience,
                                     final String connectorUUID,
                                     final Date expiryDate) {
        return Jwts.builder()
                   .setIssuer(connectorUUID)
                   .setSubject(connectorUUID)
                   .claim("@context", "https://w3id.org/idsa/contexts/context.jsonld")
                   .claim("@type", "ids:DatRequestToken")
                   .setExpiration(expiryDate)
                   .setIssuedAt(Date.from(Instant.now().minusSeconds(SECONDS_TO_SUBTRACT)))
                   .setAudience(targetAudience)
                   .setNotBefore(Date.from(Instant.now().minusSeconds(SECONDS_TO_SUBTRACT)));
    }

    /**
     * Getting PrivateKey from KeyStoreManager.
     *
     * @param keyStoreManager The KeyStoreManager holding the PrivateKey
     * @return The PrivateKey of the KeyStoreManager
     */
    private PrivateKey getPrivateKey(final KeyStoreManager keyStoreManager) {
        if (log.isDebugEnabled()) {
            log.debug("Getting PrivateKey from KeyStoreManager");
        }

        return keyStoreManager.getPrivateKey();
    }
}
