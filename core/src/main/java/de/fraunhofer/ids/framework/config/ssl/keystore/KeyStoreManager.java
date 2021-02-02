package de.fraunhofer.ids.framework.config.ssl.keystore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.IntStream;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.ids.framework.config.ssl.truststore.TrustStoreManager;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * The KeyStoreManager loads the IDSKeyStore and provides the TrustManager
 */
@Getter
public class KeyStoreManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreManager.class);

    private ConfigurationModel configurationModel;
    private KeyStore           keyStore;
    private char[]             keyStorePw;
    private String             keyAlias;
    private KeyStore           trustStore;
    private char[]             trustStorePw;
    private PrivateKey         privateKey;
    private Certificate        cert;
    private X509TrustManager   trustManager;

    public TrustStoreManager trustStoreManager = new TrustStoreManager();

    /**
     * Build the KeyStoreManager from the given configuration
     *
     * @param configurationModel a ConfigurationModel
     * @param keystorePw         the password for the IDSKeyStore
     * @param trustStorePw       the password for the IDSTrustStore
     * @param keyAlias           the alias of the IDS PrivateKey
     *
     * @throws KeyStoreManagerInitializationException when the KeyStoreManager cannot be initialized
     */
    public KeyStoreManager( ConfigurationModel configurationModel, char[] keystorePw, char[] trustStorePw,
                            String keyAlias ) throws KeyStoreManagerInitializationException {
        try {
            LOGGER.debug("Initializing KeyStoreManager");
            initClassVars(configurationModel, keystorePw, trustStorePw, keyAlias);
        } catch( IOException e ) {
            throwKeyStoreIntiException(e, "Key- or Truststore could not be loaded!");
        } catch( CertificateException | NoSuchAlgorithmException e ) {
            throwKeyStoreIntiException(e, "Error while loading a Certificate!");
        } catch( UnrecoverableKeyException e ) {
            throwKeyStoreIntiException(e, "Could not initialize Key/Truststore: password is incorrect!");
        } catch( KeyStoreException e ) {
            throwKeyStoreIntiException(e, "Initialization of Key- or Truststore failed!");
        }
    }

    /**
     * Getter for the expiration date of the Cert in the KeyStore
     *
     * @return expiration of currently used IDS Certificate
     */
    public Date getCertExpiration() {
        return ( (X509Certificate) cert ).getNotAfter();
    }

    private void throwKeyStoreIntiException( Exception e, String message )
            throws KeyStoreManagerInitializationException {
        LOGGER.error(message);
        LOGGER.error(e.getMessage(), e);
        throw new KeyStoreManagerInitializationException(e.getMessage(), e.getCause());
    }

    private void initClassVars( ConfigurationModel configurationModel, char[] keystorePw, char[] trustStorePw,
                                String keyAlias ) throws
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            KeyStoreException,
            UnrecoverableKeyException {
        this.configurationModel = configurationModel;
        this.keyStorePw = keystorePw;
        this.trustStorePw = trustStorePw;
        this.keyAlias = keyAlias;

        createKeyStore(configurationModel, keystorePw);
        createTrustStore(configurationModel, trustStorePw);
        initTrustManager(trustStorePw);
        getPrivateKeyFromKeyStore(keyAlias);
    }

    private void initTrustManager( char[] trustStorePw )
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        var myManager = loadTrustManager(trustStorePw);
        trustManager = trustStoreManager.configureTrustStore(myManager);
    }

    private void createTrustStore( ConfigurationModel configurationModel, char[] trustStorePw )
            throws CertificateException, NoSuchAlgorithmException, IOException {
        trustStore = loadKeyStore(trustStorePw, configurationModel.getTrustStore());
    }

    private void createKeyStore( ConfigurationModel configurationModel, char[] keystorePw )
            throws CertificateException, NoSuchAlgorithmException, IOException {
        keyStore = loadKeyStore(keystorePw, configurationModel.getKeyStore());
    }

    /**
     * Load a KeyStore from the given location and open it with the given password.
     * Try to find it inside the jar first, if nothing is found there, try the path at system scope
     *
     * @param pw       password of the keystore
     * @param location path of the keystore
     *
     * @return the IdsKeyStore as java keystore instance
     *
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     * @throws IOException              when the Key-/Truststore File cannot be found
     */
    private KeyStore loadKeyStore( char[] pw, URI location )
            throws CertificateException, NoSuchAlgorithmException, IOException {
        LOGGER.info(String.format("Searching for keystore file %s", location.toString()));
        KeyStore store = getKeyStoreInstance();

        if( store == null ) { return null; }

        var pathString = Paths.get(location).toString();

        //remove leading /, \ and . from path
        pathString = pathString.chars().dropWhile(value -> IntStream.of('\\', '/', '.').anyMatch(v -> v == value))
                               .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                               .toString();
        LOGGER.info("Path: " + pathString);

        var keyStoreOnClassPath = new ClassPathResource(pathString).exists();

        if( keyStoreOnClassPath ) {
            LOGGER.info("Loading KeyStore from ClassPath...");
            var is = new ClassPathResource(pathString).getInputStream();
            try {
                store.load(is, pw);
                is.close();
            } catch( IOException e ) {
                LOGGER.warn("Could not find keystore, aborting!");
                throw e;
            }
        } else {
            LOGGER.warn("Could not load keystore from classpath, trying to find it at system scope!");
            try {
                LOGGER.info(pathString);
                var fis = new FileInputStream(pathString);
                store.load(fis, pw);
                fis.close();
            } catch( IOException e ) {
                LOGGER.warn("Could not find keystore at system scope, aborting!");
                throw e;
            }
        }
        LOGGER.debug("Keystore loaded");
        return store;
    }

    @Nullable
    private KeyStore getKeyStoreInstance() {
        KeyStore store = null;
        try {
            store = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch( KeyStoreException e ) {
            LOGGER.error("Could not create a KeyStore with default type!");
            LOGGER.error(e.getMessage(), e);
        }
        return store;
    }

    /**
     * Load the TrustManager from the truststore
     *
     * @param password password of the truststore
     *
     * @return the X509TrustManager for the certificates inside the Truststore
     *
     * @throws NoSuchAlgorithmException  if no Provider supports a TrustManagerFactorySpi implementation for the specified algorithm
     * @throws UnrecoverableKeyException if the key cannot be recovered (e.g. the given password is wrong)
     * @throws KeyStoreException         if initialization of the trustmanager fails
     */
    private X509TrustManager loadTrustManager( char[] password )
            throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        LOGGER.debug("Loading trustmanager");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(this.trustStore, password);
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(this.trustStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        LOGGER.info("Trustmanager loaded");
        if( trustManagers.length != 1 || !( trustManagers[0] instanceof X509TrustManager ) ) {
            throw new IllegalStateException(
                    "Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    /**
     * Get the PrivateKey from the KeyStore (use the Key with the given alias)
     *
     * @param keyAlias the alias of the PrivateKey to be loaded
     *
     * @throws UnrecoverableKeyException if the Key cannot be retrieved from the keystore (e.g. the given password is wrong)
     * @throws NoSuchAlgorithmException  if the algorithm for recovering the key cannot be found
     * @throws KeyStoreException         if KeyStore was not initialized
     */
    private void getPrivateKeyFromKeyStore( String keyAlias )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        LOGGER.debug(String.format("Getting private key %s from keystore", keyAlias));
        Key key = keyStore.getKey(keyAlias, keyStorePw);
        if( key instanceof PrivateKey ) {
            LOGGER.debug("Setting private key and connector certificate");
            this.privateKey = (PrivateKey) key;
            this.cert = keyStore.getCertificate(keyAlias);
        }
    }
}
