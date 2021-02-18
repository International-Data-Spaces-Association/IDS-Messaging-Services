package de.fraunhofer.ids.framework.daps.aisec;

import java.io.IOException;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.ConnectorMissingCertExtensionException;
import de.fraunhofer.ids.framework.daps.DapsConnectionException;
import de.fraunhofer.ids.framework.daps.DapsEmptyResponseException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
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
@ConditionalOnProperty( prefix = "daps", name = "mode", havingValue = "aisec" )
public class AisecTokenManagerService implements de.fraunhofer.ids.framework.daps.TokenManagerService {
    public static final int SECONDS_TO_ADD = 86400;
    public static final int SECONDS_TO_SUBTRACT = 10;

    private final ClientProvider clientProvider;
    private final ConfigContainer configContainer;

    /**
     * Constructor of AisecTokenManagerService
     * @param clientProvider The clientProvider
     * @param configContainer The configContainer
     */
    public AisecTokenManagerService( final ClientProvider clientProvider, final ConfigContainer configContainer ) {
        this.clientProvider = clientProvider;
        this.configContainer = configContainer;
    }

    /***
     * Beautyfies Hex strings and will generate a result later used to create the client id (XX:YY:ZZ)
     *
     * @param hexString HexString to be beautified
     * @return beautifiedHex result
     */
    private static String beautifyHex( final String hexString ) {
        return Arrays.stream(split(hexString, 2))
                     .map(s -> s + ":")
                     .collect(Collectors.joining());
    }

    /***
     * Split string every splitEvery chars and return string array
     *
     * @param src a string that will be split into multiple substrings
     * @param splitEvery number of chars per resulting string
     * @return Array of strings resulting from splitting the input string every splitEvery chars
     */
    private static String[] split( final String src, final int splitEvery ) {
        var result = new String[(int) Math.ceil((double) src.length() / (double) splitEvery)];
        for( int i = 0; i < result.length; i++ ) {
            result[i] = src.substring(i * splitEvery, Math.min(src.length(), ( i + 1 ) * splitEvery));
        }
        return result;
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
            // get private key
            log.debug("Getting PrivateKey and Certificate from KeyStoreManager");
            var privKey = keyStoreManager.getPrivateKey();
            // Get certificate of public key
            var cert = (X509Certificate) keyStoreManager.getCert();

            // Get AKI
            //GET 2.5.29.14	SubjectKeyIdentifier / 2.5.29.35	AuthorityKeyIdentifier
            log.debug("Get AKI from certificate");
            var akiOid = Extension.authorityKeyIdentifier.getId();
            byte[] rawAuthorityKeyIdentifier = cert.getExtensionValue(akiOid);
            if( rawAuthorityKeyIdentifier == null ) {
                throw new ConnectorMissingCertExtensionException("AKI of the Connector Certificate is null!");
            }
            var akiOc = ASN1OctetString.getInstance(rawAuthorityKeyIdentifier);
            var aki = AuthorityKeyIdentifier.getInstance(akiOc.getOctets());
            var authorityKeyIdentifier = aki.getKeyIdentifier();

            //GET SKI
            log.debug("Get SKI from certificate");
            var skiOid = Extension.subjectKeyIdentifier.getId();
            var rawSubjectKeyIdentifier = cert.getExtensionValue(skiOid);
            if( rawSubjectKeyIdentifier == null ) {
                throw new ConnectorMissingCertExtensionException("SKI of the Connector Certificate is null!");
            }
            var ski0c = ASN1OctetString.getInstance(rawSubjectKeyIdentifier);
            var ski = SubjectKeyIdentifier.getInstance(ski0c.getOctets());
            var subjectKeyIdentifier = ski.getKeyIdentifier();

            var akiResult = beautifyHex(encodeHexString(authorityKeyIdentifier).toUpperCase());
            var skiResult = beautifyHex(encodeHexString(subjectKeyIdentifier).toUpperCase());

            var connectorUUID = skiResult + "keyid:" + akiResult.substring(0, akiResult.length() - 1);

            log.info("ConnectorUUID: " + connectorUUID);
            log.info("Retrieving Dynamic Attribute Token...");

            // create signed JWT (JWS)
            // Create expiry date one day (86400 seconds) from now
            log.debug("Building jwt token");
            var expiryDate = Date.from(Instant.now().plusSeconds(SECONDS_TO_ADD));
            var jwtb =
                    Jwts.builder()
                        .setIssuer(connectorUUID)
                        .setSubject(connectorUUID)
                        .claim("@context", "https://w3id.org/idsa/contexts/context.jsonld")
                        .claim("@type", "ids:DatRequestToken")
                        .setExpiration(expiryDate)
                        .setIssuedAt(Date.from(Instant.now().minusSeconds(SECONDS_TO_SUBTRACT)))
                        .setAudience(targetAudience)
                        .setNotBefore(Date.from(Instant.now().minusSeconds(SECONDS_TO_SUBTRACT)));

            log.debug("Signing jwt token");
            var jws = jwtb.signWith(SignatureAlgorithm.RS256, privKey).compact();
            log.info("Request token: " + jws);

            // build form body to embed client assertion into post request
            RequestBody formBody =
                    new FormBody.Builder()
                            .add("grant_type", "client_credentials")
                            .add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                            .add("client_assertion", jws)
                            .add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
                            .build();

            log.debug("Getting idsutils client");
            var client = clientProvider.getClient();

            var request = new Request.Builder().url(dapsUrl).post(formBody).build();

            log.debug(String.format("Sending request to %s", dapsUrl));
            var jwtResponse = client.newCall(request).execute();
            if( !jwtResponse.isSuccessful() ) {
                log.debug("DAPS request was not successful");
                throw new IOException("Unexpected code " + jwtResponse);
            }
            var responseBody = jwtResponse.body();
            if( responseBody == null ) {
                throw new DapsEmptyResponseException("JWT response is null.");
            }
            var jwtString = responseBody.string();
            log.info("Response body of token request:\n{}", jwtString);

            var jsonObject = new JSONObject(jwtString);
            dynamicAttributeToken = jsonObject.getString("access_token");

            log.info("Dynamic Attribute Token: " + dynamicAttributeToken);

        } catch( IOException e ) {
            var error = String.format("DAPS Token Manager Service - Error retrieving token and connecting to DAPS: %s",
                                      e.getMessage());
            log.error(error);

            if( configContainer.getConfigurationModel().getConnectorDeployMode() != ConnectorDeployMode.TEST_DEPLOYMENT ) {
                throw new DapsConnectionException(error);
            }
        } catch( DapsEmptyResponseException e ) {
            var error = String.format("DAPS Token Manager Service - Empty DAPS Response, something went wrong: %s",
                                      e.getMessage());
            log.error(error);

            if( configContainer.getConfigurationModel().getConnectorDeployMode() != ConnectorDeployMode.TEST_DEPLOYMENT ) {
                throw new DapsEmptyResponseException(error);
            }
        } catch( ConnectorMissingCertExtensionException e ) {
            var error = "DAPS Token Manager Service - Certificate of the Connector is missing aki/ski extensions!";
            log.error(error);

            if( configContainer.getConfigurationModel().getConnectorDeployMode() != ConnectorDeployMode.TEST_DEPLOYMENT ) {
                throw new ConnectorMissingCertExtensionException(error);
            }
        }
        return dynamicAttributeToken;
    }
}
