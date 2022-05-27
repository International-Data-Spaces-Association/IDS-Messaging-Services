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
package ids.messaging.core.config.ssl.truststore;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import lombok.NoArgsConstructor;

/**
 * The IDSTrustStore contains the trusted certificates.
 */
@NoArgsConstructor
public class TrustStoreManager {
    /**
     * Create a merged trustmanager (trust anchors are TrustStore + java Truststore combined).
     *
     * @param myTrustManager The IDS truststore.
     * @return A new truststore merging the IDS and Java Truststores.
     * @throws NoSuchAlgorithmException If default Truststore cannot be loaded.
     * @throws KeyStoreException If default Truststore cannot be loaded.
     */
    public X509TrustManager configureTrustStore(final X509TrustManager myTrustManager)
            throws NoSuchAlgorithmException, KeyStoreException {
        final var jreTrustManager = findDefaultTrustManager();
        return createMergedTrustManager(jreTrustManager, myTrustManager);
    }

    /**
     * Find the default system trustmanager.
     *
     * @return The default java truststore.
     * @throws NoSuchAlgorithmException If default Truststore cannot be loaded.
     * @throws KeyStoreException If default Truststore cannot be loaded.
     */
    private X509TrustManager findDefaultTrustManager()
            throws
            NoSuchAlgorithmException,
            KeyStoreException {
        final var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        final KeyStore blank = null;

        tmf.init(blank);
        // If keyStore is null, tmf will be
        // initialized with the default jvm trust store

        for (final var tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        return null;
    }

    /**
     * Create a merged trustmanager from 2 given trustmanagers.
     *
     * @param jreTrustManager The jre truststore.
     * @param customTrustManager The custom ids truststore.
     * @return A new truststore which will check the IDS Truststore and the
     * default java truststore for certificates.
     */
    private X509TrustManager createMergedTrustManager(final X509TrustManager jreTrustManager,
                                                      final X509TrustManager customTrustManager) {
        return new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // If you're planning to use client-cert auth,
                // merge results from "defaultTm" and "myTm".
                return jreTrustManager.getAcceptedIssuers();
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                    throws CertificateException {
                //if custom trustmanager does not work, just use jre trustmanager
                try {
                    customTrustManager.checkServerTrusted(chain, authType);
                } catch (CertificateException e) {
                    // This will throw another CertificateException if this fails too.
                    jreTrustManager.checkServerTrusted(chain, authType);
                }
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                    throws CertificateException {
                // If you're planning to use client-cert auth,
                // do the same as checking the server.
                jreTrustManager.checkClientTrusted(chain, authType);
            }
        };
    }
}
