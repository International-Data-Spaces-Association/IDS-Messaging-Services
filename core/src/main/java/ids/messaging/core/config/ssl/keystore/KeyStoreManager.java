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
package ids.messaging.core.config.ssl.keystore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.fraunhofer.iais.eis.ConfigurationModel;
import ids.messaging.core.config.ssl.truststore.TrustStoreManager;
import ids.messaging.core.config.util.CertificateSubjectCnProvider;
import ids.messaging.core.config.util.ConnectorFingerprintProvider;
import ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * The KeyStoreManager loads the IDSKeyStore and provides the TrustManager.
 */
@Slf4j
@Getter
public class KeyStoreManager {

    /**
     * The TrustStoreManager.
     */
    @Setter
    private TrustStoreManager trustStoreManager = new TrustStoreManager();

    /**
     * The ConfigurationModel.
     */
    private ConfigurationModel configurationModel;

    /**
     * The keystore.
     */
    private KeyStore keyStore;

    /**
     * The keystore password.
     */
    private char[] keyStorePw;

    /**
     * The alias.
     */
    private String keyAlias;

    /**
     * The truststore.
     */
    private KeyStore trustStore;

    /**
     * The truststore password.
     */
    private char[] trustStorePw;

    /**
     * The private key.
     */
    private PrivateKey privateKey;

    /**
     * The Certificate.
     */
    private Certificate cert;

    /**
     * The X509TrustManager.
     */
    private X509TrustManager trustManager;

    /**
     * Build the KeyStoreManager from the given configuration.
     *
     * @param configurationModel A ConfigurationModel.
     * @param keystorePw The password for the IDSKeyStore.
     * @param trustStorePw The password for the IDSTrustStore.
     * @param keyAlias The alias of the IDS PrivateKey.
     * @throws KeyStoreManagerInitializationException When the KeyStoreManager cannot be initialized
     */
    public KeyStoreManager(final ConfigurationModel configurationModel,
                           final char[] keystorePw,
                           final char[] trustStorePw,
                           final String keyAlias) throws KeyStoreManagerInitializationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing KeyStoreManager... [code=(IMSCOD0090)]");
            }
            initClassVars(configurationModel, keystorePw, trustStorePw, keyAlias);
        } catch (IOException e) {
            throwKeyStoreInitException(e, "Key- or Truststore could not be loaded!"
                                          + " [code=(IMSCOE0010)]");
        } catch (CertificateException | NoSuchAlgorithmException e) {
            throwKeyStoreInitException(e, "Error while loading a Certificate! [code=(IMSCOE0011)]");
        } catch (UnrecoverableKeyException e) {
            throwKeyStoreInitException(e,
               "Could not initialize Key/Truststore: password is incorrect! [code=(IMSCOE0012)]");
        } catch (KeyStoreException e) {
            throwKeyStoreInitException(e, "Initialization of Key- or Truststore failed!"
                                        + " [code=(IMSCOE0013)]");
        }
    }

    /**
     * Getter for the expiration date of the Cert in the KeyStore.
     *
     * @return Expiration of currently used IDS Certificate.
     */
    public Date getCertExpiration() {
        return ((X509Certificate) cert).getNotAfter();
    }

    private void throwKeyStoreInitException(final Exception exception, final String message)
            throws KeyStoreManagerInitializationException {
        if (log.isErrorEnabled()) {
            log.error(message);
        }
        throw new KeyStoreManagerInitializationException(exception.getMessage(),
                                                         exception.getCause());
    }

    private void initClassVars(final ConfigurationModel configurationModel,
                               final char[] keystorePw,
                               final char[] trustStorePw,
                               final String keyAlias) throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreException,
            UnrecoverableKeyException,
            KeyStoreManagerInitializationException {
        this.configurationModel = configurationModel;
        this.keyStorePw = keystorePw;
        this.trustStorePw = trustStorePw;
        this.keyAlias = keyAlias;

        createKeyStore(configurationModel, keystorePw);
        createTrustStore(configurationModel, trustStorePw);
        initTrustManager(trustStorePw);
        getPrivateKeyFromKeyStore(keyAlias);
        initCertificateSubjectCn(); //requires valid connector certificate (e.g. issued by DAPS)
        initCertificateConnectorFingerprint();
    }

    private void initCertificateSubjectCn()  {
        try {
            final var certificate = (X509Certificate) this.getCert();
            final var x500name = new JcaX509CertificateHolder(certificate).getSubject();
            final var cn = x500name.getRDNs(BCStyle.CN)[0];

            //Set certificate subject cn
            CertificateSubjectCnProvider.certificateSubjectCn
                    = UUID.fromString(IETFUtils.valueToString(cn.getFirst().getValue()));
        } catch (Exception exception) {
            if (log.isDebugEnabled()) {
                log.debug("Could not read Subject-CN UUID from the connector certificate."
                         + " Valid connector certificate? Will generate random UUID."
                         + " [code=(IMSCOD0091)]");
            }

            CertificateSubjectCnProvider.certificateSubjectCn = UUID.randomUUID();
        }
    }

    /**
     * Initialises the ConnectorFingerprintProvider fingerprint value (aki/ski) of the connector
     * by the connector certificate.
     */
    private void initCertificateConnectorFingerprint() {
        try {
            ConnectorFingerprintProvider.fingerprint = Optional.of(getConnectorFingerprint());
            if (log.isDebugEnabled()) {
                log.debug("Determined AKI/SKI fingerprint of the connector. [code=(IMSCOD0150),"
                          + " fingerprint=({})]",
                         ConnectorFingerprintProvider.fingerprint.get());
            }
        } catch (Exception exception) {
            ConnectorFingerprintProvider.fingerprint = Optional.empty();
            if (log.isDebugEnabled()) {
                log.debug("Could not determine AKI/SKI fingerprint of the connector."
                         + " [code=(IMSCOD0151), error=({})]", exception.getMessage());
            }
        }
    }

    private void initTrustManager(final char... trustStorePw)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        final var myManager = loadTrustManager(trustStorePw);
        trustManager = trustStoreManager.configureTrustStore(myManager);
    }

    private void createTrustStore(final ConfigurationModel configurationModel,
                                  final char... trustStorePw)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreManagerInitializationException {
        trustStore = loadKeyStore(trustStorePw,
                                  configurationModel.getTrustStore(),
                                  "Truststore");
    }

    private void createKeyStore(final ConfigurationModel configurationModel,
                                final char... keystorePw)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreManagerInitializationException {
        keyStore = loadKeyStore(keystorePw, configurationModel.getKeyStore(), "Keystore");
    }

    /**
     * Load a KeyStore from the given location and open it with the given password.
     * Try to find it inside the jar first, if nothing is found there, try the path at system scope.
     *
     * @param pw Password of the keystore.
     * @param location Path of the keystore.
     * @param keyStoreType The type of the keystore (Keystore / Truststore).
     * @return The IdsKeyStore as java keystore instance.
     * @throws CertificateException If any of the certificates in the keystore could not be loaded.
     * @throws NoSuchAlgorithmException If the algorithm used to check the integrity of the
     * keystore cannot be found.
     * @throws IOException When the Key-/Truststore File cannot be found.
     */
    private KeyStore loadKeyStore(final char[] pw, final URI location, final String keyStoreType)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreManagerInitializationException {

        validateLocation(location, keyStoreType);

        if (log.isDebugEnabled()) {
            log.debug("Searching for keystore file. [code=(IMSCOD0092), location=({})]",
                      location.toString());
        }
        final var store = getKeyStoreInstance();

        if (store == null) {
            return null;
        }

        final var pathString = Paths.get(location).toString();

        //remove leading /, \ and . from path
        final var relativepathString = pathString.chars().dropWhile(
                value -> IntStream.of('\\', '/', '.')
                                  .anyMatch(v -> v == value))
                                  .collect(StringBuilder::new,
                                          StringBuilder::appendCodePoint,
                                          StringBuilder::append)
                                  .toString();

        if (log.isDebugEnabled()) {
            log.debug("Determined relative path. [code=(IMSCOD0093), path=({})]",
                      relativepathString);
        }

        final var keyStoreOnClassPath = new ClassPathResource(relativepathString).exists();

        if (keyStoreOnClassPath) {
            if (log.isDebugEnabled()) {
                log.debug("Loading KeyStore from ClassPath... [code=(IMSCOD0094)]");
            }
            final var is = new ClassPathResource(relativepathString).getInputStream();
            try {
                store.load(is, pw);
                is.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Could not find {} aborting! [code=(IMSCOE0014),"
                              + " exception=({})]", keyStoreType, e.getMessage());
                }
                throwKeyStoreInitException(e, e.getMessage());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load keystore from classpath, trying"
                    + " to find it at system scope! [code=(IMSCOD0095)]");
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("System Path [code=(IMSCOD0096), path=({})]", pathString);
                }

                //try absolute path
                final var fileOnSystemScope = new File(pathString).exists();

                final FileInputStream fis;
                if (fileOnSystemScope) {
                    //if file at absolute path exists
                    fis = new FileInputStream(pathString);
                } else {
                    //last try, relative path in system scope
                    fis = new FileInputStream(relativepathString);
                }

                store.load(fis, pw);
                fis.close();

            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Could not find {} at system scope, aborting! [code=(IMSCOE0015),"
                              + " exception=({})]", keyStoreType, e.getMessage());
                }
                throwKeyStoreInitException(e, e.getMessage());
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Successfully loaded {}. [code=(IMSCOI0049)]", keyStoreType);
        }
        return store;
    }

    /**
     * Checks whether the keystore path specification is set in the connector configuration.
     *
     * @param location The keystore path.
     * @param keyStoreType Indication whether it is keystore or truststore.
     * @throws KeyStoreManagerInitializationException Thrown if location specification is not set.
     */
    private void validateLocation(final URI location, final String keyStoreType)
            throws KeyStoreManagerInitializationException {
        if (location == null) {
            if (log.isErrorEnabled()) {
                log.error("Location input for keystore-path from connector configuration"
                          + " is not valid!"
                         + " [code=(IMSCOE0016), type=({}), location=(null)]", keyStoreType);
            }
            throw new KeyStoreManagerInitializationException(
                    "Location input for keystore-path is null! Type: " + keyStoreType);
        }
    }

    @Nullable
    private KeyStore getKeyStoreInstance() {
        KeyStore store = null;
        try {
            store = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            if (log.isErrorEnabled()) {
                log.error("Could not create a KeyStore with default type! [code=(IMSCOE0017),"
                          + "exception=({})]", e.getMessage());
            }
        }
        return store;
    }

    /**
     * Load the TrustManager from the truststore.
     *
     * @param password Password of the truststore.
     * @return The X509TrustManager for the certificates inside the Truststore.
     * @throws NoSuchAlgorithmException If no Provider supports a TrustManagerFactorySpi
     * implementation for the specified algorithm.
     * @throws UnrecoverableKeyException If the key cannot be recovered
     * (e.g. the given password is wrong).
     * @throws KeyStoreException If initialization of the trustmanager fails.
     */
    private X509TrustManager loadTrustManager(final char... password)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        if (log.isDebugEnabled()) {
            log.debug("Loading trustmanager... [code=(IMSCOD0097)]");
        }

        final var keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(this.trustStore, password);

        final var trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(this.trustStore);
        final var trustManagers = trustManagerFactory.getTrustManagers();

        if (log.isDebugEnabled()) {
            log.debug("Successfully loaded the trustmanager using the TrustStore."
                      + " [code=(IMSCOD0098)]");
        }

        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust"
                + " managers:" + Arrays.toString(trustManagers));
        }

        return (X509TrustManager) trustManagers[0];
    }

    /**
     * Get the PrivateKey from the KeyStore (use the Key with the given alias).
     *
     * @param keyAlias The alias of the PrivateKey to be loaded.
     * @throws UnrecoverableKeyException If the Key cannot be retrieved
     * from the keystore (e.g. the given password is wrong).
     * @throws NoSuchAlgorithmException If the algorithm for recovering the key cannot be found.
     * @throws KeyStoreException If KeyStore was not initialized or private key cannot be found.
     */
    private void getPrivateKeyFromKeyStore(final String keyAlias)
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Getting private key from keystore. [code=(IMSCOD0098), alias=({})]",
                      keyAlias);
        }
        final var key = keyStore.getKey(keyAlias, keyStorePw);

        if (key == null) {
            if (log.isErrorEnabled()) {
                log.error("Keystoremanager: No private key for the given alias found"
                          + " within the Keystore! Given alias does not exist"
                          + " or does not identify a key-related entry! [code=(IMSCOE0018),"
                          + "alias=({})]", keyAlias);
            }
            throw new KeyStoreException("Keystoremanager: No private key for the given alias found"
                                        + " within the Keystore! Given alias does not exist"
                                        + " or does not identify a key-related entry! [alias=("
                                        + keyAlias + ")]");
        }

        if (key instanceof PrivateKey) {
            if (log.isDebugEnabled()) {
                log.debug("Setting private key and connector certificate. [code=(IMSCOD0099)]");
            }
            this.privateKey = (PrivateKey) key;
            this.cert = keyStore.getCertificate(keyAlias);
        }
    }

    /**
     * Generates the fingerprint of the Connector (UID) using the KeyStoreManager.
     *
     * @return The generated connector fingerprint (UID).
     * @throws ConnectorMissingCertExtensionException Thrown if either AKI
     * or SKI are not valid within the connector certificate.
     */
    private String getConnectorFingerprint()
            throws ConnectorMissingCertExtensionException {
        final var certificate = (X509Certificate) this.getCert();
        final var authorityKeyIdentifier = getCertificateAKI(certificate);
        final var subjectKeyIdentifier = getCertificateSKI(certificate);

        return generateConnectorFingerprint(authorityKeyIdentifier, subjectKeyIdentifier);
    }

    /**
     * Generates the fingerprint of the connector (UID).
     *
     * @param authorityKeyIdentifier The connector certificate AKI.
     * @param subjectKeyIdentifier The connector certificate SKI.
     * @return The generated fingerprint of the connector (UID).
     */
    private String generateConnectorFingerprint(final byte[] authorityKeyIdentifier,
                                                final byte[] subjectKeyIdentifier) {
        final var akiResult = beautifyHex(encodeHexString(authorityKeyIdentifier).toUpperCase());
        final var skiResult = beautifyHex(encodeHexString(subjectKeyIdentifier).toUpperCase());

        return skiResult + "keyid:" + akiResult.substring(0, akiResult.length() - 1);
    }

    /**
     * Get the SKI of the certificate.
     *
     * @param cert The X509Certificate-Certificate
     * @return The SKI-KeyIdentifier of the certificate
     * @throws ConnectorMissingCertExtensionException thrown if SKI of certificate is empty
     */
    private byte[] getCertificateSKI(final X509Certificate cert)
            throws ConnectorMissingCertExtensionException {
        if (log.isDebugEnabled()) {
            log.debug("Get SKI from certificate... [code=(IMSCOD0110)]");
        }

        final var skiOid = Extension.subjectKeyIdentifier.getId();
        final var rawSubjectKeyIdentifier = cert.getExtensionValue(skiOid);

        if (rawSubjectKeyIdentifier == null) {
            throw new ConnectorMissingCertExtensionException(
                    "SKI of the Connector Certificate is null!");
        }

        final var ski0c = ASN1OctetString.getInstance(rawSubjectKeyIdentifier);
        final var ski = SubjectKeyIdentifier.getInstance(ski0c.getOctets());

        return ski.getKeyIdentifier();
    }

    /**
     * Get the AKI of the certificate.
     *
     * @param cert The X509Certificate-Certificate
     * @return The AKI-KeyIdentifier of the Certificate
     * @throws ConnectorMissingCertExtensionException thrown if AKI of certificate is empty
     */
    private byte[] getCertificateAKI(final X509Certificate cert)
            throws ConnectorMissingCertExtensionException {
        if (log.isDebugEnabled()) {
            log.debug("Get AKI from certificate... [code=(IMSCOD0111)]");
        }

        final var akiOid = Extension.authorityKeyIdentifier.getId();
        final var rawAuthorityKeyIdentifier = cert.getExtensionValue(akiOid);

        checkEmptyRawAKI(rawAuthorityKeyIdentifier); //can throw exception

        final var akiOc = ASN1OctetString.getInstance(rawAuthorityKeyIdentifier);
        final var aki = AuthorityKeyIdentifier.getInstance(akiOc.getOctets());

        return aki.getKeyIdentifier();
    }

    /**
     * Checks if AKI is empty.
     *
     * @param rawAuthorityKeyIdentifier The AKI to check
     * @throws ConnectorMissingCertExtensionException thrown if AKI of certificate is null
     */
    private void checkEmptyRawAKI(final byte[] rawAuthorityKeyIdentifier)
            throws ConnectorMissingCertExtensionException {
        if (rawAuthorityKeyIdentifier == null) {
            throw new ConnectorMissingCertExtensionException(
                    "AKI of the Connector Certificate is null!");
        }
    }

    /**
     * Beautifies Hex strings and will generate a result later used to
     * create the client id (XX:YY:ZZ).
     *
     * @param hexString HexString to be beautified
     * @return beautifiedHex result
     */
    private static String beautifyHex(final String hexString) {
        return Arrays.stream(hexString.split("(?<=\\G..)"))
                     .map(s -> s + ":")
                     .collect(Collectors.joining());
    }
}
