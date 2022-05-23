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
package ids.messaging.core.daps.orbiter;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ids.messaging.core.config.ClientProvider;
import ids.messaging.core.daps.TokenManagerService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Manages Dynamic Attribute Tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "daps", name = "mode", havingValue = "orbiter")
public class OrbiterTokenManagerService implements TokenManagerService {

    /**
     * The chosen keysize.
     */
    public static final int KEYSIZE = 2048;

    /**
     * Seconds to subtract for issued at to avoid time problems.
     */
    public static final int SECONDS_TO_SUBTRACT = 10;

    /**
     * The default client registration URL.
     */
    private static final String CLIENT_REGISTRATION_URL
        = "https://orbiter-daps-staging.truzzt.org/api/client/create";

    /**
     * Used for reading all necessary bytes of the response.
     */
    private static final int BYTESINT = 0xFF;

    /**
     * The ClientProvider.
     */
    private final ClientProvider clientProvider;

    /**
     * The certificate.
     */
    private X509Certificate clientCert;

    /**
     * The generated keypair used.
     */
    private KeyPair generatedKeyPair;

    /**
     * Client-ID for the request body.
     */
    private String id;

    /**
     * Create a Client at the Orbiter DAPS, request a DAT and return it.
     */
    public void createClient() {
        final var client = clientProvider.getClient();
        try {
            setKeyPair();

            final var csr = createCSR();

            //write csr to string
            final var csrString = getCertificateRequest(csr);

            //build request
            final var request = getCertificateRequestMessage(csrString);

            //parse client response
            final var dapsResponse = sendCertificateRequest(client, request);

            final var responseJson = new JSONObject(dapsResponse);

            setClientId(responseJson);
            setClientCertificate(responseJson);

        } catch (NoSuchAlgorithmException
                | OperatorCreationException
                | IOException
                | CertificateException e) {
            if (log.isWarnEnabled()) {
                log.warn("[code=(IMSCOW0028)] " + e.getMessage(), e);
            }
        }
    }

    private void setClientCertificate(final JSONObject responseJson)
            throws CertificateException {
        final var responseCert = responseJson.getJSONObject("cert");
        final var responseData = responseCert.getJSONArray("data");
        var bytes = new byte[responseData.length()];

        for (var i = 0; i < responseData.length(); i++) {
            bytes[i] = (byte) (((int) responseData.get(i)) & BYTESINT);
        }

        final var certFactory = CertificateFactory.getInstance("X.509");
        final var in = new ByteArrayInputStream(bytes);

        clientCert = (X509Certificate) certFactory.generateCertificate(in);
    }

    private void setClientId(final JSONObject responseJson) {
        id = responseJson.getString("id");
    }

    private String sendCertificateRequest(final OkHttpClient client,
                                          final Request request)
            throws IOException {
        final var response = client.newCall(request).execute();

        return Objects.requireNonNull(response.body()).string();
    }

    private Request getCertificateRequestMessage(final String csrString) {
        final var clientToCreate = new JSONObject();
        clientToCreate.put("grants", List.of("client_assertion_type"));
        clientToCreate.put("pkcs10Data", csrString);
        clientToCreate.put("userId", "TEST");

        return new Request.Builder()
                .url(CLIENT_REGISTRATION_URL)
                .post(RequestBody.create(clientToCreate.toString(),
                                         MediaType.parse("application/json")))
                .build();
    }

    private String getCertificateRequest(final PKCS10CertificationRequest csr)
            throws IOException {
        final var pemObject = new PemObject("CERTIFICATE REQUEST", csr.getEncoded());
        final var stringWriter = new StringWriter();

        final var pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        stringWriter.close();

        return stringWriter.toString();
    }

    private void setKeyPair() throws NoSuchAlgorithmException {
        final var keyPairGenerator =
                KeyPairGenerator.getInstance("RSA", new BouncyCastleProvider());
        keyPairGenerator.initialize(KEYSIZE, new SecureRandom());

        generatedKeyPair = keyPairGenerator.generateKeyPair();
    }

    /**
     * Generate a CSR which is sent to the Orbiter DAPS to register a Client.
     *
     * @return a generated CSR
     * @throws OperatorCreationException when the ContentSigner
     * cannot be created
     * @throws IOException when the Extensions cannot be added to the CSR
     */
    private PKCS10CertificationRequest createCSR()
            throws IOException, OperatorCreationException {
        //create csr builder with principal
        final var p10Builder = new JcaPKCS10CertificationRequestBuilder(
                new X500Principal("C=DE, ST=Bonn, L=NRW, O=truzzt, CN=*.truzzt.org"),
                         generatedKeyPair.getPublic());

        //add extensions
        final var extensionsGenerator = new ExtensionsGenerator();

        //basic constraints = false
        extensionsGenerator.addExtension(Extension.basicConstraints, false,
                                         new BasicConstraints(false));

        //add subject alternative names
        final var sans = new ASN1Encodable[]{
                new GeneralName(GeneralName.dNSName, "*.truzzt.org"),
                new GeneralName(GeneralName.dNSName, "*.truzzt.com")
        };

        final var sansExtension = new DERSequence(sans);
        extensionsGenerator
                .addExtension(Extension.subjectAlternativeName,
                              false,
                              sansExtension);

        //TODO add SKI extension but currently working without it)
        //extensionsGenerator.addExtension(Extension.subjectKeyIdentifier,
        // true, new SubjectKeyIdentifier());

        final var extensions = extensionsGenerator.generate();
        p10Builder.addAttribute(
                PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                extensions);

        //create csBuilder for signing the request
        final var csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        final var signer = csBuilder.build(generatedKeyPair.getPrivate());

        //build and return the csr
        return p10Builder.build(signer);
    }

    /**
     * Get the DAT Token from Orbiter DAPS.
     *
     * @param dapsUrl The URL of the DAPS
     * @return access jwt token as String
     */
    @Override
    public String acquireToken(final String dapsUrl) {
        final var client = clientProvider.getClient();

        //register at Orbiter DAPS if this is called the first time
        if (id == null || generatedKeyPair == null || clientCert == null) {
            //does this have to happen in the framework or should we expect
            // a keystore provided by the user (like Aisec Daps Client)
            createClient();
        }

        final var token = getRequestToken();
        final var formBody = getRequestBody(token);
        final var request = getRequestMessage(dapsUrl, formBody);

        try {
            return getDAT(client, request);
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("[code=(IMSCOW0029)] " + e.getMessage(), e);
            }

            return "";
        }
    }

    private String getDAT(
            final OkHttpClient client,
            final Request request)
            throws IOException {
        final var jwtResponse = client.newCall(request).execute();
        final var responseJson = new JSONObject(
                Objects.requireNonNull(jwtResponse.body()).string());

        return responseJson.getString("accessToken");
    }

    private Request getRequestMessage(final String dapsUrl,
                                      final RequestBody formBody) {
        return new Request.Builder()
                .url(dapsUrl)
                .post(formBody)
                .build();
    }

    private RequestBody getRequestBody(final String token) {
        return new FormBody.Builder()
                .add("grant_type", "client_assertion_type")
                .add("client_assertion_type",
                     "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                .add("client_assertion", token)
                .add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
                .add("client_id", id)
                .build();
    }

    private String getRequestToken() {
        final var jwtb =
                Jwts.builder()
                    .setIssuer("localhost")
                    .setSubject(id)
                    .claim("@context",
                           "https://w3id.org/idsa/contexts/context.jsonld")
                    .claim("@type", "ids:DatRequestToken")
                    .setExpiration(
                            Date.from(Instant.now().plus(Duration.ofDays(1))))
                    .setAudience("all")
                    .setIssuedAt(Date.from(
                            Instant.now().minusSeconds(SECONDS_TO_SUBTRACT)))
                    .setNotBefore(Date.from(
                            Instant.now().minusSeconds(SECONDS_TO_SUBTRACT)));

        return jwtb.signWith(SignatureAlgorithm.RS256,
                             generatedKeyPair.getPrivate()).compact();
    }
}
