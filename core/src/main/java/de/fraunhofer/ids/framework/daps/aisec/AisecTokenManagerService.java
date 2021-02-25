package de.fraunhofer.ids.framework.daps.aisec;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import de.fraunhofer.ids.framework.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.framework.daps.DapsConnectionException;
import de.fraunhofer.ids.framework.daps.DapsEmptyResponseException;
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
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * Manages Dynamic Attribute Tokens.
 *
 * @author Gerd Brost (gerd.brost@aisec.fraunhofer.de)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty( prefix = "daps", name = "mode", havingValue = "aisec" )
public class AisecTokenManagerService implements de.fraunhofer.ids.framework.daps.TokenManagerService {
    public static final int ONE_DAY_IN_SECONDS  = 86400;
    public static final int SECONDS_TO_SUBTRACT = 10;

    private final ClientProvider  clientProvider;
    private final ConfigContainer configContainer;

    /***
     * Beautyfies Hex strings and will generate a result later used to create the client id (XX:YY:ZZ).
     *
     * @param hexString HexString to be beautified
     * @return beautifiedHex result
     */
    private static String beautifyHex( final String hexString ) {
        return Arrays.stream(hexString.split("(?<=\\G..)"))
                     .map(s -> s + ":")
                     .collect(Collectors.joining());
    }

    /**
     * Get the DAT from the DAPS at dapsURL using the current configuration
     *
     * @param dapsUrl The URL of a DAPS Service
     *
     * @return signed DAPS JWT token for the Connector
     */
    public String acquireToken( final String dapsUrl )
            throws DapsConnectionException, DapsEmptyResponseException, ConnectorMissingCertExtensionException {

        var keyStoreManager = configContainer.getKeyStoreManager();
        var dynamicAttributeToken = "INVALID_TOKEN";
        var targetAudience = "idsc:IDS_CONNECTORS_ALL";

        // Try clause for setup phase (loading keys, building trust manager)
        try {
            var privateKey = getPrivateKey(keyStoreManager);
            var connectorUUID = getConnectorUUID(keyStoreManager);

            log.info("ConnectorUUID: " + connectorUUID);
            log.info("Retrieving Dynamic Attribute Token...");

            var jws = getRequestToken(targetAudience, privateKey, connectorUUID);
            log.info("Request token: " + jws);

            // build form body to embed client assertion into post request
            var formBody = getFormBody(jws);

            log.debug("Getting client");
            var client = clientProvider.getClient();

            var request = new Request.Builder().url(dapsUrl).post(formBody).build();

            log.debug(String.format("Sending request to %s", dapsUrl));
            var jwtResponse = sendRequestToDAPS(client, request);
            var responseBody = jwtResponse.body();
            checkEmptyDAPSResponse(responseBody); //can throw exception

            var jwtString = responseBody.string();
            log.info("Response body of token request:\n{}", jwtString);

            dynamicAttributeToken = getDAT(jwtString);

        } catch( IOException e ) {
            handleIOException(e);
        } catch( DapsEmptyResponseException e ) {
            handleDapsEmptyResponseException(e);
        } catch( ConnectorMissingCertExtensionException e ) {
            handleConnectorMissingCertExtensionException();
        }

        return dynamicAttributeToken;
    }

    /**
     * Handle exception if AKI or SKI of Certificate are not valid or missing
     *
     * @throws ConnectorMissingCertExtensionException forwarded to the connector developer if AKI or SKI of Certificate are not valid or missing
     */
    private void handleConnectorMissingCertExtensionException() throws ConnectorMissingCertExtensionException {
        var error = "DAPS Token Manager Service - Certificate of the Connector is missing aki/ski extensions!";
        log.error(error);

        if( configContainer.getConfigurationModel().getConnectorDeployMode()
            != ConnectorDeployMode.TEST_DEPLOYMENT ) {
            throw new ConnectorMissingCertExtensionException(error);
        }
    }

    /**
     * Handle exception if DAPS returned an empty response
     *
     * @param e the thrown excpetion
     *
     * @throws DapsEmptyResponseException forwarded exception to the connector developer if DAPS returned an empty response
     */
    private void handleDapsEmptyResponseException( DapsEmptyResponseException e ) throws DapsEmptyResponseException {
        var error = String.format("DAPS Token Manager Service - Empty DAPS Response, something went wrong: %s",
                                  e.getMessage());
        log.error(error);

        if( configContainer.getConfigurationModel().getConnectorDeployMode()
            != ConnectorDeployMode.TEST_DEPLOYMENT ) {
            throw new DapsEmptyResponseException(error);
        }
    }

    /**
     * Handle exception if connection to DAPS failed
     *
     * @param e the thrwon IOException
     *
     * @throws DapsConnectionException mapped exception, thworn if connection to DAPS failed
     */
    private void handleIOException( IOException e ) throws DapsConnectionException {
        var error = String.format("DAPS Token Manager Service - Error retrieving token and connecting to DAPS: %s",
                                  e.getMessage());
        log.error(error);

        if( configContainer.getConfigurationModel().getConnectorDeployMode()
            != ConnectorDeployMode.TEST_DEPLOYMENT ) {
            throw new DapsConnectionException(error);
        }
    }

    /**
     * Get the DAT of the response from DAPS
     *
     * @param jwtString the DAPS response
     *
     * @return the DAT
     */
    private String getDAT( String jwtString ) {
        var jsonObject = new JSONObject(jwtString);
        var dynamicAttributeToken = jsonObject.getString("access_token");
        log.info("Dynamic Attribute Token: " + dynamicAttributeToken);
        return dynamicAttributeToken;
    }

    /**
     * DAPS didn't return a response
     *
     * @param responseBody the response of the DAPS to be checked
     *
     * @throws DapsEmptyResponseException thrown if response of DAPS is empty
     */
    private void checkEmptyDAPSResponse( ResponseBody responseBody ) throws DapsEmptyResponseException {
        if( responseBody == null ) {
            throw new DapsEmptyResponseException("JWT response is null.");
        }
    }

    /**
     * Sends the request for a DAT to the DAPS
     *
     * @param client  the HTTP-Client used for sending messages
     * @param request the request-message to be send
     *
     * @return the response of the DAPS
     *
     * @throws IOException thrown if something went wrong connecting to the DAPS
     */
    @NotNull
    private Response sendRequestToDAPS( OkHttpClient client, Request request ) throws IOException {
        var jwtResponse = client.newCall(request).execute();
        if( !jwtResponse.isSuccessful() ) {
            log.debug("DAPS request was not successful");
            throw new IOException("Unexpected code " + jwtResponse);
        }
        return jwtResponse;
    }

    /**
     * Get the form body for the DAPS-Request
     *
     * @param jws the generated JWS
     *
     * @return The Request-Formbody
     */
    @NotNull
    private FormBody getFormBody( String jws ) {
        return new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                .add("client_assertion", jws)
                .add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
                .build();
    }

    /**
     * Get the signed(!) request token
     *
     * @param targetAudience The target audience
     * @param privateKey     the private key of the keystore
     * @param connectorUUID  the UUID of the Connector
     *
     * @return The signed request token
     */
    private String getRequestToken( String targetAudience, PrivateKey privateKey, String connectorUUID ) {
        log.debug("Building jwt token");
        var expiryDate = Date.from(Instant.now().plusSeconds(ONE_DAY_IN_SECONDS));
        var jwtb = getJwtBuilder(targetAudience, connectorUUID, expiryDate);
        log.debug("Signing jwt token");
        return jwtb.signWith(SignatureAlgorithm.RS256, privateKey).compact();
    }

    /**
     * Get the JWT Builder
     *
     * @param targetAudience The targetAudience
     * @param connectorUUID  The UUID of the Connector
     * @param expiryDate     The set expiry date
     *
     * @return The JWT-Builder
     */
    private JwtBuilder getJwtBuilder( String targetAudience, String connectorUUID, Date expiryDate ) {
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
     * Generated the UUID of the Connector by giving the method only the KeyStoreManager
     *
     * @param keyStoreManager The KeyStoremanager used to access the AKI and SKI of the Certificate
     *
     * @return The generated Connector-UUID
     *
     * @throws ConnectorMissingCertExtensionException Thrown if either AKI or SKI are not valid within the Connector-Certificate
     */
    @NotNull
    private String getConnectorUUID( KeyStoreManager keyStoreManager ) throws ConnectorMissingCertExtensionException {
        var certificate = getCertificate(keyStoreManager);
        var authorityKeyIdentifier = getCertificateAKI(certificate);
        var subjectKeyIdentifier = getCertificateSKI(certificate);
        return generateConnectorUUID(authorityKeyIdentifier, subjectKeyIdentifier);
    }

    /**
     * Generates the UUID of the Connector
     *
     * @param authorityKeyIdentifier The Connector-Certificate AKI
     * @param subjectKeyIdentifier   The Connector-Certificate SKI
     *
     * @return The generated UUID of the Connector
     */
    @NotNull
    private String generateConnectorUUID( byte[] authorityKeyIdentifier, byte[] subjectKeyIdentifier ) {
        var akiResult = beautifyHex(encodeHexString(authorityKeyIdentifier).toUpperCase());
        var skiResult = beautifyHex(encodeHexString(subjectKeyIdentifier).toUpperCase());

        return skiResult + "keyid:" + akiResult.substring(0, akiResult.length() - 1);
    }

    /**
     * Get the SKI of the certificate
     *
     * @param cert The X509Certificate-Certificate
     *
     * @return The SKI-KeyIdentifier of the certificate
     *
     * @throws ConnectorMissingCertExtensionException thrwon if SKI of certificateis empty
     */
    private byte[] getCertificateSKI( X509Certificate cert ) throws ConnectorMissingCertExtensionException {
        log.debug("Get SKI from certificate");
        var skiOid = Extension.subjectKeyIdentifier.getId();
        var rawSubjectKeyIdentifier = cert.getExtensionValue(skiOid);
        if( rawSubjectKeyIdentifier == null ) {
            throw new ConnectorMissingCertExtensionException("SKI of the Connector Certificate is null!");
        }
        var ski0c = ASN1OctetString.getInstance(rawSubjectKeyIdentifier);
        var ski = SubjectKeyIdentifier.getInstance(ski0c.getOctets());
        return ski.getKeyIdentifier();
    }

    /**
     * Get the AKI of the certificate
     *
     * @param cert The X509Certificate-Certificate
     *
     * @return The AKI-KeyIdentifier of the Certificate
     *
     * @throws ConnectorMissingCertExtensionException thrown if AKI of certificate is empty
     */
    private byte[] getCertificateAKI( X509Certificate cert ) throws ConnectorMissingCertExtensionException {
        log.debug("Get AKI from certificate");
        var akiOid = Extension.authorityKeyIdentifier.getId();
        var rawAuthorityKeyIdentifier = cert.getExtensionValue(akiOid);
        checkEmptyRawAKI(rawAuthorityKeyIdentifier); //can throw exception
        var akiOc = ASN1OctetString.getInstance(rawAuthorityKeyIdentifier);
        var aki = AuthorityKeyIdentifier.getInstance(akiOc.getOctets());
        return aki.getKeyIdentifier();
    }

    /**
     * Checks if AKI is empty
     *
     * @param rawAuthorityKeyIdentifier The AKI to check
     *
     * @throws ConnectorMissingCertExtensionException thrown if AKI of certificate is null
     */
    private void checkEmptyRawAKI( byte[] rawAuthorityKeyIdentifier ) throws ConnectorMissingCertExtensionException {
        if( rawAuthorityKeyIdentifier == null ) {
            throw new ConnectorMissingCertExtensionException("AKI of the Connector Certificate is null!");
        }
    }

    /**
     * Getting Certificate from KeyStoreManager
     *
     * @param keyStoreManager The KeyStoreManager holding the Certificate
     *
     * @return The Certificate of the KeyStoreManager
     */
    private X509Certificate getCertificate( KeyStoreManager keyStoreManager ) {
        log.debug("Getting Certificate from KeyStoreManager");
        return (X509Certificate) keyStoreManager.getCert();
    }

    /**
     * Getting PrivateKey from KeyStoreManager
     *
     * @param keyStoreManager The KeyStoreManager holding the PrivateKey
     *
     * @return The PrivateKey of the KeyStoreManager
     */
    private PrivateKey getPrivateKey( KeyStoreManager keyStoreManager ) {
        log.debug("Getting PrivateKey from KeyStoreManager");
        return keyStoreManager.getPrivateKey();
    }
}
