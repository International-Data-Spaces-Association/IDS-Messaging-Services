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
import java.util.UUID;
import java.util.stream.IntStream;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.ids.messaging.core.config.ssl.truststore.TrustStoreManager;
import de.fraunhofer.ids.messaging.core.config.util.CertificateSubjectCnProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;

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
     * @param keyAlias The alias of the IDS PrivateKey.
     * @throws KeyStoreManagerInitializationException When the KeyStoreManager cannot be initialized
     */
    public KeyStoreManager(final ConfigurationModel configurationModel,
                           final String keyAlias) throws KeyStoreManagerInitializationException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing KeyStoreManager");
            }
            if(configurationModel.getKeyStorePassword() == null) {
                throwKeyStoreInitException("No KeyStore password provided!");
            }
            final char[] keystorePw = configurationModel.getKeyStorePassword().toCharArray();
            char[] trustStorePw = new char[]{};
            if(configurationModel.getTrustStore() != null){
                if(configurationModel.getTrustStorePassword() == null) {
                    throwKeyStoreInitException("TrustStore is specified, but password is empty!");
                }
                trustStorePw = configurationModel.getTrustStorePassword().toCharArray();
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

    private void throwKeyStoreInitException(final String message)
            throws KeyStoreManagerInitializationException  {
        if (log.isErrorEnabled()) {
            log.error(message);
        }
        throw new KeyStoreManagerInitializationException(message);
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
        if(configurationModel.getTrustStore() != null) {
            createTrustStore(configurationModel, trustStorePw);
        }
        initTrustManager(trustStorePw);
        getPrivateKeyFromKeyStore(keyAlias);
        initCertificateSubjectCn(); //requires valid connector certificate (e.g. issued by DAPS)
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
                         + " Valid connector certificate? Will generate random UUID.");
            }

            CertificateSubjectCnProvider.certificateSubjectCn = UUID.randomUUID();
        }
    }

    private void initTrustManager(final char... trustStorePw)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        if(trustStore != null) {
            final var myManager = loadTrustManager(trustStorePw);
            trustManager = trustStoreManager.configureTrustStore(myManager);
        }
        trustManager = trustStoreManager.configureTrustStore(null);
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
        if (log.isDebugEnabled()) {
            log.debug("Searching for keystore file. [location=({})]", location.toString());
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
            log.debug("Determined relative path. [path=({})]", relativepathString);
        }

        final var keyStoreOnClassPath = new ClassPathResource(relativepathString).exists();

        if (keyStoreOnClassPath) {
            if (log.isDebugEnabled()) {
                log.debug("Loading KeyStore from ClassPath ...");
            }
            final var is = new ClassPathResource(relativepathString).getInputStream();
            try {
                store.load(is, pw);
                is.close();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Could not find {} aborting! [exception=({})]", keyStoreType,
                              e.getMessage());
                }
                throwKeyStoreInitException(e, e.getMessage());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load keystore from classpath, trying"
                    + " to find it at system scope!");
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("System Path [path=({})]", pathString);
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
                    log.error("Could not find {} at system scope, aborting! [exception=({})]",
                              keyStoreType, e.getMessage());
                }
                throwKeyStoreInitException(e, e.getMessage());
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Successfully loaded {}.", keyStoreType);
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
                log.error("Could not create a KeyStore with default type! [exception=({})]",
                          e.getMessage());
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
            log.debug("Loading trustmanager...");
        }

        final var keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(this.trustStore, password);

        final var trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(this.trustStore);
        final var trustManagers = trustManagerFactory.getTrustManagers();

        if (log.isDebugEnabled()) {
            log.debug("Successfully loaded the trustmanager using the TrustStore.");
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
            log.debug("Getting private key from keystore. [alias=({})]", keyAlias);
        }
        final var key = keyStore.getKey(keyAlias, keyStorePw);

        if (key == null) {
            if (log.isErrorEnabled()) {
                log.error("Keystoremanager: No private key for the given alias found"
                          + " within the Keystore! Given alias does not exist"
                          + " or does not identify a key-related entry! [alias=({})]",
                          keyAlias);
            }
            throw new KeyStoreException("Keystoremanager: No private key for the given alias found"
                                        + " within the Keystore! Given alias does not exist"
                                        + " or does not identify a key-related entry! [alias=("
                                        + keyAlias + ")]");
        }

        if (key instanceof PrivateKey) {
            if (log.isDebugEnabled()) {
                log.debug("Setting private key and connector certificate");
            }
            this.privateKey = (PrivateKey) key;
            this.cert = keyStore.getCertificate(keyAlias);
        }
    }
}
