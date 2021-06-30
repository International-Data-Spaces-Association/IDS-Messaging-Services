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
import java.util.stream.IntStream;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.ids.messaging.core.config.ssl.truststore.TrustStoreManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;

/**
 * The KeyStoreManager loads the IDSKeyStore and provides the TrustManager.
 */
@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeyStoreManager {

    @Setter
    TrustStoreManager trustStoreManager = new TrustStoreManager();

    ConfigurationModel configurationModel;
    KeyStore           keyStore;
    char[]             keyStorePw;
    String             keyAlias;
    KeyStore           trustStore;
    char[]             trustStorePw;
    PrivateKey         privateKey;
    Certificate        cert;
    X509TrustManager   trustManager;

    /**
     * Build the KeyStoreManager from the given configuration.
     *
     * @param configurationModel a ConfigurationModel
     * @param keystorePw         the password for the IDSKeyStore
     * @param trustStorePw       the password for the IDSTrustStore
     * @param keyAlias           the alias of the IDS PrivateKey
     * @throws KeyStoreManagerInitializationException when the KeyStoreManager cannot be initialized
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
            throwKeyStoreInitException(e, "Could not initialize Key/Truststore: password is incorrect!");
        } catch (KeyStoreException e) {
            throwKeyStoreInitException(e, "Initialization of Key- or Truststore failed!");
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
            log.error(message + " " + exception.getMessage());
        }
        throw new KeyStoreManagerInitializationException(exception.getMessage(), exception.getCause());
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
    }

    private void initTrustManager(final char... trustStorePw)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        final var myManager = loadTrustManager(trustStorePw);
        trustManager = trustStoreManager.configureTrustStore(myManager);
    }

    private void createTrustStore(final ConfigurationModel configurationModel, final char... trustStorePw)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreManagerInitializationException {
        trustStore = loadKeyStore(trustStorePw, configurationModel.getTrustStore());
    }

    private void createKeyStore(final ConfigurationModel configurationModel, final char... keystorePw)
            throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreManagerInitializationException {
        keyStore = loadKeyStore(keystorePw, configurationModel.getKeyStore());
    }

    /**
     * Load a KeyStore from the given location and open it with the given password.
     * Try to find it inside the jar first, if nothing is found there, try the path at system scope
     *
     * @param pw       password of the keystore
     * @param location path of the keystore
     * @return the IdsKeyStore as java keystore instance
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     * @throws IOException              when the Key-/Truststore File cannot be found
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

        var pathString = Paths.get(location).toString();

        //remove leading /, \ and . from path
        pathString = pathString.chars().dropWhile(value -> IntStream.of('\\', '/', '.').anyMatch(v -> v == value))
                               .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                               .toString();

        if (log.isInfoEnabled()) {
            log.info("Path: " + pathString);
        }

        final var keyStoreOnClassPath = new ClassPathResource(pathString).exists();

        if (keyStoreOnClassPath) {
            if (log.isInfoEnabled()) {
                log.info("Loading KeyStore from ClassPath...");
            }
            final var is = new ClassPathResource(pathString).getInputStream();
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
                log.warn("Could not load keystore from classpath, trying to find it at system scope!");
            }
            try {
                if (log.isInfoEnabled()) {
                    log.info(pathString);
                }
                final var fis = new FileInputStream(pathString);
                store.load(fis, pw);
                fis.close();
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not find keystore at system scope, aborting!");
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
     * @throws NoSuchAlgorithmException  if no Provider supports a TrustManagerFactorySpi implementation for the specified algorithm
     * @throws UnrecoverableKeyException if the key cannot be recovered (e.g. the given password is wrong)
     * @throws KeyStoreException         if initialization of the trustmanager fails
     */
    private X509TrustManager loadTrustManager(final char... password)
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        if (log.isDebugEnabled()) {
            log.debug("Loading trustmanager");
        }

        final var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(this.trustStore, password);

        final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(this.trustStore);
        final var trustManagers = trustManagerFactory.getTrustManagers();

        if (log.isInfoEnabled()) {
            log.info("Trustmanager loaded");
        }

        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }

        return (X509TrustManager) trustManagers[0];
    }

    /**
     * Get the PrivateKey from the KeyStore (use the Key with the given alias).
     *
     * @param keyAlias the alias of the PrivateKey to be loaded
     *
     * @throws UnrecoverableKeyException if the Key cannot be retrieved from the keystore (e.g. the given password is wrong)
     * @throws NoSuchAlgorithmException  if the algorithm for recovering the key cannot be found
     * @throws KeyStoreException         if KeyStore was not initialized
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
}
