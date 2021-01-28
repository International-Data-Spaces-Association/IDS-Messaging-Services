package de.fraunhofer.ids.framework.daps;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import okhttp3.*;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * Manages Dynamic Attribute Tokens.
 */
public class TokenManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenManagerService.class);

    private static SSLSocketFactory sslSocketFactory;

    /**
     * Get the DAT from the DAPS at dapsURL using the current configuration
     *
     * @param container An IDS Connector Configuration
     * @param dapsUrl   The URL of a DAPS Service
     * @param provider  providing underlying OkHttpClient
     *
     * @return signed DAPS JWT (JWS) token for the Connector
     */
    public static String acquireToken( ConfigContainer container, ClientProvider provider, String dapsUrl ) {
        var keyStoreManager = container.getKeyManager();
        var dynamicAttributeToken = "INVALID_TOKEN";
        var targetAudience = "idsc:IDS_CONNECTORS_ALL";

        try {
            //GET Certificate and PrivateKey
            LOGGER.debug("Getting PrivateKey and Certificate from KeyStoreManager");
            Key privKey = keyStoreManager.getPrivateKey();
            X509Certificate cert = (X509Certificate) keyStoreManager.getCert(); // Get certificate of public key

            //GET AKI (AuthorityKeyIdentifier)
            LOGGER.debug("Get AKI from certificate");
            var akiId = Extension.authorityKeyIdentifier.getId();
            var rawAuthorityKeyIdentifier = cert.getExtensionValue(akiId);
            verifyAKIIdentifier(rawAuthorityKeyIdentifier);

            var aki = AuthorityKeyIdentifier
                    .getInstance(ASN1OctetString.getInstance(rawAuthorityKeyIdentifier).getOctets());
            var authorityKeyIdentifier = aki.getKeyIdentifier();

            //GET SKI (SubjectKeyIdentifier)
            LOGGER.debug("Get SKI from certificate");
            var skiId = Extension.subjectKeyIdentifier.getId();
            var rawSubjectKeyIdentifier = cert.getExtensionValue(skiId);
            verifySKIIdentifier(rawSubjectKeyIdentifier);

            var ski0c = ASN1OctetString.getInstance(rawSubjectKeyIdentifier);
            var ski = SubjectKeyIdentifier.getInstance(ski0c.getOctets());
            var subjectKeyIdentifier = ski.getKeyIdentifier();

            //GET connectorUUID (using AKI & SKI)
            var akiResult = beautifyHex(encodeHexString(authorityKeyIdentifier).toUpperCase());
            var skiResult = beautifyHex(encodeHexString(subjectKeyIdentifier).toUpperCase());
            var connectorUUID = skiResult + "keyid:" + akiResult.substring(0, akiResult.length() - 1);
            LOGGER.info("ConnectorUUID: " + connectorUUID);

            //GET DAT (Dynamic Attribute Token)
            LOGGER.info("Retrieving DAT (Dynamic Attribute Token)...");
            LOGGER.debug("Building JWT (JSON Web Token)...");
            var expiryDate = Date.from(Instant.now().plusSeconds(86400)); // expiry date one day from now
            var jwtBuilder =
                    Jwts.builder()
                        .setIssuer(connectorUUID)
                        .setSubject(connectorUUID)
                        .claim("@context", "https://w3id.org/idsa/contexts/context.jsonld")
                        .claim("@type", "ids:DatRequestToken")
                        .setExpiration(expiryDate)
                        .setIssuedAt(Date.from(Instant.now().minusSeconds(10)))
                        .setAudience(targetAudience)
                        .setNotBefore(Date.from(Instant.now().minusSeconds(10)));
            LOGGER.debug("Signing JWT token -> JWS (JSON Web Signature)");
            var jws = jwtBuilder.signWith(SignatureAlgorithm.RS256, privKey).compact();
            LOGGER.info("Request token: " + jws);

            //build form body to embed client assertion into post request
            RequestBody formBody =
                    new FormBody.Builder()
                            .add("grant_type", "client_credentials")
                            .add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                            .add("client_assertion", jws)
                            .add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
                            .build();

            //build Request and send Token
            var client = provider.getClient();
            Request request = new Request.Builder().url(dapsUrl + "/v2/token").post(formBody).build();
            LOGGER.debug(String.format("Sending request to %s", dapsUrl + "/v2/token"));
            Response jwtResponse = client.newCall(request).execute();
            verifyJWTResponse(jwtResponse);

            var responseBody = jwtResponse.body();
            verifyJWTResponseBody(responseBody);
            var jwtString = responseBody.string();
            LOGGER.info("Response body of token request:\n{}", jwtString);
            var jsonObject = new JSONObject(jwtString);
            dynamicAttributeToken = jsonObject.getString("access_token");

            LOGGER.info("Dynamic Attribute Token: " + dynamicAttributeToken);
        } catch( IOException e ) {
            LOGGER.error(String.format("Error retrieving token: %s", e.getMessage()));
        } catch( EmptyDapsResponseException e ) {
            LOGGER.error(String.format("Something else went wrong: %s", e.getMessage()));
        } catch( MissingCertExtensionException e ) {
            LOGGER.error("Certificate of the Connector is missing aki/ski extensions!");
        }

        return dynamicAttributeToken;
    }

    private static void verifyJWTResponseBody( ResponseBody responseBody ) throws EmptyDapsResponseException {
        if( responseBody == null ) {
            throw new EmptyDapsResponseException("JWT response is null.");
        }
    }

    private static void verifyJWTResponse( Response jwtResponse ) throws IOException {
        if( !jwtResponse.isSuccessful() ) {
            LOGGER.debug("DAPS request was not successful");
            throw new IOException("Unexpected code " + jwtResponse);
        }
    }

    private static void verifySKIIdentifier( byte[] rawSubjectKeyIdentifier ) throws MissingCertExtensionException {
        if( rawSubjectKeyIdentifier == null ) {
            throw new MissingCertExtensionException("SKI of the Connector Certificate is null!");
        }
    }

    private static void verifyAKIIdentifier( byte[] rawAuthorityKeyIdentifier ) throws MissingCertExtensionException {
        if( rawAuthorityKeyIdentifier == null ) {
            throw new MissingCertExtensionException("AKI of the Connector Certificate is null!");
        }
    }

    /***
     * Beautyfies Hex strings and will generate a result later used to create the client id (XX:YY:ZZ)
     *
     * @param hexString HexString to be beautified
     * @return beautifiedHex result
     */
    private static String beautifyHex( String hexString ) {
        return Arrays.stream(split(hexString, 2))
                     .map(s -> s + ":")
                     .collect(Collectors.joining());
    }

    /***
     * Split string every n chars and return string array
     *
     * @param src a string that will be split into multiple substrings
     * @param n number of chars per resulting string
     * @return Array of strings resulting from splitting the input string every n chars
     */
    public static String[] split( String src, int n ) {
        var result = new String[(int) Math.ceil((double) src.length() / (double) n)];
        for( int i = 0; i < result.length; i++ ) {
            result[i] = src.substring(i * n, Math.min(src.length(), ( i + 1 ) * n));
        }
        return result;
    }
}
