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
package de.fraunhofer.ids.messaging.core.config.ssl.keystore;

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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.ids.messaging.core.config.ssl.truststore.TrustStoreManager;
import de.fraunhofer.ids.messaging.core.config.util.ConnectorUUIDProvider;
import de.fraunhofer.ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.jetbrains.annotations.NotNull;
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
     * The Connector UUID.
     */
    private String connectorUUID;

    /**
     * Build the KeyStoreManager from the given configuration.
     *
     * @param configurationModel A ConfigurationModel.
     * @param keystorePw The password for the IDSKeyStore.
     * @param trustStorePw The password for the IDSTrustStore.
     * @param keyAlias The alias of the IDS PrivateKey.
     * @throws KeyStoreManagerInitializationException When the KeyStoreManager
     * cannot be initialized.
     */
    public KeyStoreManager(final ConfigurationModel configurationModel,
                           final char[] keystorePw,
                           final char[] trustStorePw,
                           final String keyAlias) throws KeyStoreManagerInitializationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing KeyStoreManager");
            }
            initClassVars(configurationModel, keystorePw, trustStorePw, keyAlias);
        } catch (IOException e) {
            throwKeyStoreInitException(e, "Key- or Truststore could not be loaded!");
        } catch (CertificateException | NoSuchAlgorithmException e) {
            throwKeyStoreInitException(e, "Error while loading a Certificate!");
        } catch (UnrecoverableKeyException e) {
            throwKeyStoreInitException(e,
               "Could not initialize Key/Truststore: password is incorrect!");
        } catch (KeyStoreException e) {
            throwKeyStoreInitException(e, "Initialization of Key- or Truststore failed!");
        } catch (ConnectorMissingCertExtensionException e) {
            if (log.isErrorEnabled()) {
                log.error("Connector UUID could not be generated because connector certificate is "
                          + "missing AKI and SKI! Will be required for DAPS communication. "
                          + "Possible Reason: You are not using a connector "
                          + "certificate provided by the DAPS (e.g. a generic testing "
                          + "certificate). Using default Connector UUID instead.");
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Connector UUID: " + ConnectorUUIDProvider.ConnertorUUID);
        }
    }

    /**
     * Getter for the expiration date of the Cert in the KeyStore.
     *
     * @return expiration of currently used IDS Certificate
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
            KeyStoreManagerInitializationException,
            ConnectorMissingCertExtensionException {
        this.configurationModel = configurationModel;
        this.keyStorePw = keystorePw;
        this.trustStorePw = trustStorePw;
        this.keyAlias = keyAlias;

        createKeyStore(configurationModel, keystorePw);
        createTrustStore(configurationModel, trustStorePw);
        initTrustManager(trustStorePw);
        getPrivateKeyFromKeyStore(keyAlias);
        generateConnectorUUID();
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
                                  configurationModel.getTrustStore());
    }

    private void createKeyStore(final ConfigurationModel configurationModel,
                                final char... keystorePw)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreManagerInitializationException {
        keyStore = loadKeyStore(keystorePw, configurationModel.getKeyStore());
    }

    /**
     * Load a KeyStore from the given location
     * and open it with the given password.
     * Try to find it inside the jar first, if nothing
     * is found there, try the path at system scope
     *
     * @param pw password of the keystore
     * @param location path of the keystore
     * @return the IdsKeyStore as java keystore instance
     * @throws CertificateException if any of the certificates
     * in the keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to
     * check the integrity of the keystore cannot be found
     * @throws IOException when the Key-/Truststore File cannot be found
     */
    private KeyStore loadKeyStore(final char[] pw, final URI location)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreManagerInitializationException {
        if (log.isInfoEnabled()) {
            log.info(String.format("Searching for keystore file %s", location.toString()));
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

        if (log.isInfoEnabled()) {
            log.info("Relative Path: " + relativepathString);
        }

        final var keyStoreOnClassPath = new ClassPathResource(relativepathString).exists();

        if (keyStoreOnClassPath) {
            if (log.isInfoEnabled()) {
                log.info("Loading KeyStore from ClassPath...");
            }
            final var is = new ClassPathResource(relativepathString).getInputStream();
            try {
                store.load(is, pw);
                is.close();
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not find keystore, aborting!");
                }
                throwKeyStoreInitException(e, e.getMessage());
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Could not load keystore from classpath, trying"
                    + " to find it at system scope!");
            }
            try {
                if (log.isInfoEnabled()) {
                    log.info("System Path: " + pathString);
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
                    log.error("Could not find keystore at system scope, aborting!");
                }
                throwKeyStoreInitException(e, e.getMessage());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Keystore loaded");
        }
        return store;
    }

    @Nullable
    private KeyStore getKeyStoreInstance() {
        KeyStore store = null;
        try {
            store = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            if (log.isErrorEnabled()) {
                log.error("Could not create a KeyStore with default type! " + e.getMessage());
            }
        }
        return store;
    }

    /**
     * Load the TrustManager from the truststore.
     *
     * @param password password of the truststore
     * @return the X509TrustManager for the certificates inside the Truststore
     * @throws NoSuchAlgorithmException if no Provider supports a
     * TrustManagerFactorySpi implementation for the specified algorithm
     * @throws UnrecoverableKeyException if the key cannot be
     * recovered (e.g. the given password is wrong)
     * @throws KeyStoreException if initialization of the trustmanager fails
     */
    private X509TrustManager loadTrustManager(final char... password)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        if (log.isDebugEnabled()) {
            log.debug("Loading trustmanager");
        }

        final var keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(this.trustStore, password);

        final var trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(this.trustStore);
        final var trustManagers = trustManagerFactory.getTrustManagers();

        if (log.isInfoEnabled()) {
            log.info("Trustmanager loaded");
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
     * @param keyAlias the alias of the PrivateKey to be loaded
     *
     * @throws UnrecoverableKeyException if the Key cannot be retrieved
     * from the keystore (e.g. the given password is wrong)
     * @throws NoSuchAlgorithmException if the algorithm
     * for recovering the key cannot be found
     * @throws KeyStoreException if KeyStore was not initialized
     */
    private void getPrivateKeyFromKeyStore(final String keyAlias)
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Getting private key %s from keystore", keyAlias));
        }
        final var key = keyStore.getKey(keyAlias, keyStorePw);

        if (key instanceof PrivateKey) {
            if (log.isDebugEnabled()) {
                log.debug("Setting private key and connector certificate");
            }
            this.privateKey = (PrivateKey) key;
            this.cert = keyStore.getCertificate(keyAlias);
        }
    }

    /**
     * Generated the UUID of the Connector by giving the method only the KeyStoreManager.
     *
     * @throws KeyStoreException Generic Keystore exception.
     * @throws ConnectorMissingCertExtensionException Thrwon if SKI of certificateis empty.
     */
    private void generateConnectorUUID()
            throws KeyStoreException, ConnectorMissingCertExtensionException {
        final var certificate = (X509Certificate) keyStore.getCertificate(keyAlias);
        final var authorityKeyIdentifier = getCertificateAKI(certificate);
        final var subjectKeyIdentifier = getCertificateSKI(certificate);

        final var connectorUUID = generateConnectorUUID(authorityKeyIdentifier, subjectKeyIdentifier);

        this.connectorUUID = connectorUUID;

        //also make connector UUID available per getter
        ConnectorUUIDProvider.ConnertorUUID = connectorUUID;
        ConnectorUUIDProvider.validUUID = true;
    }

    /**
     * Get the SKI of the certificate.
     *
     * @param cert The X509Certificate-Certificate.
     * @return The SKI-KeyIdentifier of the certificate.
     * @throws ConnectorMissingCertExtensionException Thrwon if SKI of certificateis empty.
     */
    private byte[] getCertificateSKI(final X509Certificate cert)
            throws ConnectorMissingCertExtensionException {
        if (log.isDebugEnabled()) {
            log.debug("Get SKI from certificate");
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
     * @param cert The X509Certificate-Certificate.
     * @return The AKI-KeyIdentifier of the Certificate.
     * @throws ConnectorMissingCertExtensionException Thrown if AKI of certificate is empty.
     */
    private byte[] getCertificateAKI(final X509Certificate cert)
            throws ConnectorMissingCertExtensionException {
        if (log.isDebugEnabled()) {
            log.debug("Get AKI from certificate");
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
     * @param rawAuthorityKeyIdentifier The AKI to check.
     * @throws ConnectorMissingCertExtensionException Thrown if AKI of certificate is null.
     */
    private void checkEmptyRawAKI(final byte[] rawAuthorityKeyIdentifier)
            throws ConnectorMissingCertExtensionException {
        if (rawAuthorityKeyIdentifier == null) {
            throw new ConnectorMissingCertExtensionException(
                    "AKI of the Connector Certificate is null!");
        }
    }

    /**
     * Generates the UUID of the Connector.
     *
     * @param authorityKeyIdentifier The Connector-Certificate AKI.
     * @param subjectKeyIdentifier The Connector-Certificate SKI.
     * @return The generated UUID of the Connector.
     */
    @NotNull
    private String generateConnectorUUID(final byte[] authorityKeyIdentifier,
                                         final byte[] subjectKeyIdentifier) {
        final var akiResult = beautifyHex(encodeHexString(authorityKeyIdentifier).toUpperCase());
        final var skiResult = beautifyHex(encodeHexString(subjectKeyIdentifier).toUpperCase());

        return skiResult + "keyid:" + akiResult.substring(0, akiResult.length() - 1);
    }

    /**
     * Beautyfies Hex strings and will generate a result later used to
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
