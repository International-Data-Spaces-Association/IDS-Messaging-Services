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
package ids.messaging.core.daps.aisec;

import java.io.IOException;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import ids.messaging.core.config.ClientProvider;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import ids.messaging.core.config.util.ConnectorFingerprintProvider;
import ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import ids.messaging.core.daps.DapsConnectionException;
import ids.messaging.core.daps.DapsEmptyResponseException;
import ids.messaging.core.daps.TokenManagerService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Manages DAT requests to the DAPS.
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
     * Default offset to be used for DAPS and Connector nbf and issued at dates.
     * Must also be declared here for special use cases.
     */
    public static final int DEFAULT_TIME_OFFSET = 10;

    /**
     * Seconds to subtract for the issued at and not before in the JWT to the DAPS.
     */
    @Value("#{new Integer('${daps.time.offset.seconds:10}')}")
    private Integer offset;

    /**
     * The ClientProvider.
     */
    private final ClientProvider  clientProvider;

    /**
     * The ConfigContainer.
     */
    private final ConfigContainer configContainer;

    /**
     * Used to switch logging the DAPS response on and off.
     */
    @Value("#{new Boolean('${daps.enable.log.jwt:false}')}")
    private Boolean logDapsResponse;

    /**
     * SinatureAlgorithm used for signing JWT for DAT request.
     * Supported: RSA256, ECDSA256
     */
    @Value("${daps.jwt.signature.algorithm:RSA256}")
    private String signatureAlgorithm;

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
            DapsEmptyResponseException,
            ConnectorMissingCertExtensionException {

        final var keyStoreManager = configContainer.getKeyStoreManager();
        final var targetAudience = "idsc:IDS_CONNECTORS_ALL";

        var dynamicAttributeToken = "INVALID_TOKEN";

        // Try clause for setup phase (loading keys, building trust manager)
        try {
            validateCertExpiration(keyStoreManager);

            final var privateKey = getPrivateKey(keyStoreManager);

            //aki/ski fingerprint of the connector
            final var fingerprint = ConnectorFingerprintProvider.fingerprint;
            if (fingerprint.isEmpty()) {
                throw new ConnectorMissingCertExtensionException("No fingerprint available!");
            }
            final var connectorFingerprint = fingerprint.get();

            if (log.isDebugEnabled()) {
                log.debug("Retrieving Dynamic Attribute Token from DAPS... [code=(IMSCOD0104)]");
            }

            final var jws = getRequestToken(targetAudience, privateKey, connectorFingerprint);

            // build form body to embed client assertion into post request
            final var formBody = getFormBody(jws);

            if (log.isDebugEnabled()) {
                log.debug("Getting client... [code=(IMSCOD0105)]");
            }

            final var client = clientProvider.getClient();

            if (log.isDebugEnabled()) {
                log.debug("Sending DAT request to DAPS. [code=(IMSCOD0106), url=({})]", dapsUrl);
            }
            final var request = new Request.Builder().url(dapsUrl).post(formBody).build();

            final var jwtResponse = sendRequestToDAPS(client, request);
            final var responseBody = jwtResponse.body();
            checkEmptyDAPSResponse(responseBody); //can throw exception

            final var jwtString = responseBody.string();
            dynamicAttributeToken = getDAT(jwtString);

            if (jwtResponse.isSuccessful() && log.isInfoEnabled()) {
                if (logDapsResponse) {
                    log.info("Successfully received DAT from DAPS."
                            + " [response=({}), code=(IMSCOI0053)]", jwtString);
                } else {
                    log.info("Successfully received DAT from DAPS. [code=(IMSCOI0054)]");
                }
            }
        } catch (IOException e) {
            handleIOException(e);
        } catch (DapsEmptyResponseException e) {
            handleDapsEmptyResponseException(e);
        } catch (ConnectorMissingCertExtensionException e) {
            handleConnectorMissingCertExtensionException();
        }

        return dynamicAttributeToken;
    }

    private static void validateCertExpiration(final KeyStoreManager keyStoreManager) {
        final var certExpiration = keyStoreManager.getCertExpiration();

        if (log.isDebugEnabled()) {
            log.debug("Certificate expiration: ({}) ... [code=(IMSCOI0152)]", certExpiration);
        }

        if (log.isWarnEnabled()) {
            if (certExpiration.before(new Date())) {
                log.warn("Certificate expired! [code=(IMSCOI0153), date=({})]", certExpiration);
            }
        }
    }

    /**
     * Handle exception if AKI or SKI of Certificate are not valid or missing.
     *
     * @throws ConnectorMissingCertExtensionException forwarded to the
     * connector developer if AKI or SKI of Certificate are not
     * valid or missing
     */
    private void handleConnectorMissingCertExtensionException()
            throws ConnectorMissingCertExtensionException {
        final var error = "Mandatory required information of the connector "
          + "certificate is missing (AKI/SKI)!"
          + " Needed to determine the fingerprint of the connector for the DAPS!"
          + " Possible reason: Are you using a valid connector certificate issued by the DAPS?";

        if (configContainer.getConfigurationModel().getConnectorDeployMode()
           != ConnectorDeployMode.TEST_DEPLOYMENT) {
            printProductiveDeploymentError(error);
            throw new ConnectorMissingCertExtensionException(error);
        } else {
            printTestDeploymentWarning(error);
        }
    }

    /**
     * Handle exception if DAPS returned an empty response.
     *
     * @param e the thrown exception
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
     * @param e the thrown IOException
     * @throws DapsConnectionException mapped exception, thrown if connection to DAPS failed
     */
    private void handleIOException(final IOException e) throws DapsConnectionException {
        final var error = String.format("Error connecting to DAPS "
            + "(possibly currently not reachable or wrong DAPS-URL): %s",
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
                    + "will not be sent in PRODUCTIVE_DEPLOYMENT. [code=(IMSCOW0041),"
                    + " reason=({})]", error);
        }
    }

    private void printProductiveDeploymentError(final String error) {
        if (log.isErrorEnabled()) {
            log.error(
                    "PRODUCTIVE_DEPLOYMENT: No IDS-Message sent! "
                    + "No DAT could be acquired from DAPS! [code=(IMSCOE0001),"
                    + " reason=({})]", error);
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
                log.debug("DAPS request was not successful! [code=(IMSCOD0107)]");
            }

            final var responseBody = jwtResponse.body();
            final var bodyString = responseBody == null ? "" : " Body: " + responseBody.string();
            throw new IOException("Unexpected code " + jwtResponse + bodyString);
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
     * @param targetAudience The target audience.
     * @param privateKey The private key of the keystore.
     * @param connectorFingerprint The fingerprint of the Connector (UID).
     * @return The signed request token.
     */
    private String getRequestToken(final String targetAudience,
                                   final PrivateKey privateKey,
                                   final String connectorFingerprint) {
        if (log.isDebugEnabled()) {
            log.debug("Building jwt token... [code=(IMSCOD0108)]");
        }

        final var expiryDate = Date.from(Instant.now().plusSeconds(ONE_DAY_IN_SECONDS));
        final var jwtb = getJwtBuilder(targetAudience, connectorFingerprint, expiryDate);

        if (log.isDebugEnabled()) {
            log.debug("Signing jwt token... [code=(IMSCOD0109)]");
        }

        if ("ECDSA256".equals(signatureAlgorithm)) {
            // ECDSA 256
            return jwtb.signWith(SignatureAlgorithm.ES256, privateKey).compact();
        } else {
            // Default: RSA 256
            return jwtb.signWith(SignatureAlgorithm.RS256, privateKey).compact();
        }
    }

    /**
     * Get the JWT Builder.
     *
     * @param targetAudience The targetAudience.
     * @param connectorFingerprint The fingerprint of the Connector (UID).
     * @param expiryDate The set expiry date.
     * @return The JWT-Builder.
     */
    private JwtBuilder getJwtBuilder(final String targetAudience,
                                     final String connectorFingerprint,
                                     final Date expiryDate) {

        if (offset == null) {
            offset = DEFAULT_TIME_OFFSET;
        }

        if (log.isDebugEnabled()) {
            log.debug("JWT for DAPS request: using offset seconds for issuedAt and notBefore"
                      + " [offset=({}), code=(IMSCOD0143)]", offset);
        }

        final var timeWithOffset = Date.from(Instant.now().minusSeconds(offset));

        return Jwts.builder()
                   .setIssuer(connectorFingerprint)
                   .setSubject(connectorFingerprint)
                   .claim("@context", "https://w3id.org/idsa/contexts/context.jsonld")
                   .claim("@type", "ids:DatRequestToken")
                   .setExpiration(expiryDate)
                   .setIssuedAt(timeWithOffset)
                   .setAudience(targetAudience)
                   .setNotBefore(timeWithOffset);
    }

    /**
     * Getting PrivateKey from KeyStoreManager.
     *
     * @param keyStoreManager The KeyStoreManager holding the PrivateKey
     * @return The PrivateKey of the KeyStoreManager
     */
    private PrivateKey getPrivateKey(final KeyStoreManager keyStoreManager) {
        if (log.isDebugEnabled()) {
            log.debug("Getting PrivateKey from KeyStoreManager... [code=(IMSCOD0113)]");
        }

        return keyStoreManager.getPrivateKey();
    }
}
