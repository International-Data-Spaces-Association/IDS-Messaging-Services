package de.fraunhofer.ids.framework.config.ssl.truststore;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 *  The IDSTrustStore contains the trusted certificates
 */
public class TrustStoreManager {
    /**
     * Create a merged trustmanager (trust anchors are TrustStore + java Truststore combined)
     *
     * @param myTrustManager the IDS truststore
     *
     * @return a new truststore merging the IDS and Java Truststores
     *
     * @throws NoSuchAlgorithmException if default Truststore cannot be loaded
     * @throws KeyStoreException        if default Truststore cannot be loaded
     */
    public X509TrustManager configureTrustStore( X509TrustManager myTrustManager )
            throws NoSuchAlgorithmException, KeyStoreException {
        X509TrustManager jreTrustManager = findDefaultTrustManager();
        return createMergedTrustManager(jreTrustManager, myTrustManager);
    }

    /**
     * Find the default system trustmanager
     *
     * @return the default java truststore
     *
     * @throws NoSuchAlgorithmException if default Truststore cannot be loaded
     * @throws KeyStoreException        if default Truststore cannot be loaded
     */
    private X509TrustManager findDefaultTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore blank = null;
        tmf.init(blank); // If keyStore is null, tmf will be initialized with the default jvm trust store
        for( TrustManager tm : tmf.getTrustManagers() ) {
            if( tm instanceof X509TrustManager ) {
                return (X509TrustManager) tm;
            }
        }
        return null;
    }

    /**
     * Create a merged trustmanager from 2 given trustmanagers
     *
     * @param jreTrustManager    the jre truststore
     * @param customTrustManager the custom ids truststore
     *
     * @return a new truststore which will check the IDS Truststore and the default java truststore for certificates
     */
    private X509TrustManager createMergedTrustManager( X509TrustManager jreTrustManager,
                                                       X509TrustManager customTrustManager ) {
        return new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // If you're planning to use client-cert auth,
                // merge results from "defaultTm" and "myTm".
                return jreTrustManager.getAcceptedIssuers();
            }

            @Override
            public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
                //if custom trustmanager does not work, just use jre trustmanager
                try {
                    customTrustManager.checkServerTrusted(chain, authType);
                } catch( CertificateException e ) {
                    // This will throw another CertificateException if this fails too.
                    jreTrustManager.checkServerTrusted(chain, authType);
                }
            }

            @Override
            public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
                // If you're planning to use client-cert auth,
                // do the same as checking the server.
                jreTrustManager.checkClientTrusted(chain, authType);
            }

        };
    }
}
