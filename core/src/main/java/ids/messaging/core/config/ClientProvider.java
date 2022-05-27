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
package ids.messaging.core.config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import ids.messaging.core.config.ssl.keystore.KeyStoreManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okio.GzipSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

/**
 * The ClientProvider uses the {@link ConfigContainer} to rebuild
 * clients, when a new configurationContainer is created.
 */
@Slf4j
public class ClientProvider {
    /**
     * The ConfigContainer.
     */
    private final ConfigContainer configContainer;

    /**
     * The OkHttpClient.
     */
    @Getter
    private OkHttpClient client;

    /**
     * Constructor, creating a Client provider using
     * the KeyStore part from the ConfigurationContainer.
     *
     * @param configContainer The {@link ConfigContainer} managing current configurations.
     * @throws NoSuchAlgorithmException If the cryptographic is unknown when building
     * an {@link OkHttpClient}.
     * @throws KeyManagementException If there is an error with any configured key
     * when building an {@link OkHttpClient}.
     */
    public ClientProvider(final ConfigContainer configContainer)
            throws KeyManagementException, NoSuchAlgorithmException {
        this.configContainer = configContainer;
        setClient(configContainer);
    }

    /**
     * Create the client builder, which can be used to build the
     * OkHttpClient directly, or to customize timeouts for the client.
     *
     * @param connector The current connector configuration.
     * @param manager The current key- and truststore.
     * @return An {@link okhttp3.OkHttpClient.Builder} using
     * the current configuration of the connector.
     * @throws NoSuchAlgorithmException If the cryptographic is unknown.
     * @throws KeyManagementException If there is an error with any configured key.
     */
    private static OkHttpClient.Builder createClientBuilder(final ConfigurationModel connector,
                                                            final KeyStoreManager manager)
            throws NoSuchAlgorithmException, KeyManagementException {

        final var okHttpBuilder = getOkHttpBuilder();

        if (connector.getConnectorDeployMode() == ConnectorDeployMode.PRODUCTIVE_DEPLOYMENT) {
            if (log.isDebugEnabled()) {
                log.debug("Productive Deployment, use Trustmanager from KeyStoreManager."
                          + " [code=(IMSMED0066)]");
            }

            setSSLSocketFactory(manager, okHttpBuilder);
        } else if (connector.getConnectorDeployMode() == ConnectorDeployMode.TEST_DEPLOYMENT) {
            setAcceptingAllSSLCertificates(okHttpBuilder);
        }

        if (log.isDebugEnabled()) {
            log.debug("Created SSLSocketFactory. [code=(IMSCOD0067)]");
        }

        handleConnectorProxy(connector, okHttpBuilder);

        return okHttpBuilder;
    }

    /**
     * If Connector has a proxy set.
     *
     * @param connector The Config of the Connector.
     * @param okHttpBuilder The Builder of the HTTPClient used to send messages.
     */
    private static void handleConnectorProxy(final ConfigurationModel connector,
                                             final OkHttpClient.Builder okHttpBuilder) {
        //if the connector has a proxy set
        if (connector.getConnectorProxy() != null) {
            //if there is any proxy in the proxylist
            final var proxyConfiguration = connector
                    .getConnectorProxy().stream().findAny().orElse(null);

            if (proxyConfiguration != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Proxy is set active! Configuring Proxy. [code=(IMSCOD0068)]");
                }

                //create and set Proxy Authenticator with BasicAuth if
                //proxy username and password are set
                setProxyAuthenticator(okHttpBuilder, proxyConfiguration);

                //create a custom proxySelector (will select the proxy when
                //request goes to host not in NO_PROXY list, and NO_PROXY otherwise)
                if (log.isDebugEnabled()) {
                    log.debug("Creating a ProxySelector. [code=(IMSCOD0069)]");
                }
                setProxySelector(okHttpBuilder, proxyConfiguration);
            }
        }
    }

    /**
     * Select the Proxy being used.
     *
     * @param okHttpBuilder The Builder of the okHttp Client used for sending messages.
     * @param proxyConfiguration The configuration of the proxy to be used.
     */
    private static void setProxySelector(final OkHttpClient.Builder okHttpBuilder,
                                         final de.fraunhofer.iais.eis.Proxy proxyConfiguration) {
        final var proxySelector = new ProxySelector() {
            @Override
            public List<Proxy> select(final URI uri) {

                //create a List of size 1 containing the possible Proxy
                final List<Proxy> proxyList = new ArrayList<>(1);

                if (proxyConfiguration.getNoProxy().contains(uri)) {
                    if (log.isDebugEnabled()) {
                        log.debug("URI is in NoProxy List, no proxy is used. [code=(IMSCOD0070),"
                                  + " uri=({})]", uri.toString());
                    }

                    //if the called uri is in the Exceptions of the
                    //Connectors ProxyConfiguration use no proxy
                    proxyList.add(Proxy.NO_PROXY);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("URI is not in NoProxy List, use configured Proxy."
                                  + " [code=(IMSCOD0071), uri=({})]", uri.toString());
                    }

                    //else use proxy with ProxyConfig
                    final var proxyAddress = proxyConfiguration.getProxyURI();
                    final var proxyHost = proxyAddress.getHost();
                    final int proxyPort = proxyAddress.getPort();

                    if (log.isDebugEnabled()) {
                        log.debug("Address: [code=(IMSCOD0072), host=({}), port=({})]",
                                  proxyHost, proxyPort);
                    }

                    if (proxyHost == null || proxyHost.trim().equals("")) {
                        if (log.isWarnEnabled()) {
                            log.warn("Proxy hostname invalid! Trying to skip using this proxy!"
                                     + " Please check configuration! [code=(IMSCOW0030),"
                                     + " hostname=({})]", proxyHost);
                        }
                        proxyList.add(Proxy.NO_PROXY);
                    } else if (proxyPort == -1) {
                        if (log.isWarnEnabled()) {
                            log.warn("Proxy port invalid! Trying to skip using this proxy!"
                                     + " Please check configuration! [code=(IMSCOW0031),"
                                     + " port=({})]", proxyPort);
                        }
                        proxyList.add(Proxy.NO_PROXY);
                    } else {
                        proxyList.add(new Proxy(Proxy.Type.HTTP,
                                                new InetSocketAddress(proxyHost, proxyPort)));
                    }

                }
                return proxyList;
            }

            @Override
            public void connectFailed(final URI uri,
                                      final SocketAddress sa,
                                      final IOException ioe) {
                throw new UnsupportedOperationException(
                        "The selected Proxy is unavailable!");
            }
        };

        //set proxySelector for the okhttpclient
        okHttpBuilder.proxySelector(proxySelector);
    }

    /**
     * Set the Proxy-Authenticator.
     *
     * @param okHttpBuilder The Builder of the okHttp Client used for sending messages.
     * @param proxyConfiguration The configuration of the proxy to be used.
     */
    private static void setProxyAuthenticator(
            final OkHttpClient.Builder okHttpBuilder,
            final de.fraunhofer.iais.eis.Proxy proxyConfiguration) {

        if (proxyConfiguration.getProxyAuthentication() != null
            && proxyConfiguration.getProxyAuthentication().getAuthUsername() != null
            && proxyConfiguration.getProxyAuthentication().getAuthPassword() != null) {

            if (log.isDebugEnabled()) {
                log.debug("Setting Proxy Authenticator. [code=(IMSCOD0073)]");
            }

            final Authenticator proxyAuthenticator = (route, response) -> {
                final var credential = Credentials.basic(proxyConfiguration
                                                                 .getProxyAuthentication()
                                                                 .getAuthUsername(),
                                                         proxyConfiguration
                                                                 .getProxyAuthentication()
                                                                 .getAuthPassword());
                return response.request().newBuilder()
                               .header("Proxy-Authorization", credential)
                               .build();
            };

            okHttpBuilder.proxyAuthenticator(proxyAuthenticator);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No Proxy Authentication credentials are set! [code=(IMSCOD0074)]");
            }
        }
    }

    /**
     * Used only if Connector is in Test-Deployment mode.
     *
     * @param okHttpBuilder The okHTTP-Builder used.
     * @throws NoSuchAlgorithmException Exception thrown.
     * @throws KeyManagementException Exception thrown.
     */
    private static void setAcceptingAllSSLCertificates(final OkHttpClient.Builder okHttpBuilder)
            throws NoSuchAlgorithmException, KeyManagementException {
        if (log.isWarnEnabled()) {
            log.warn("Trustmanager is trusting all Certificates in "
                     + "TEST_DEPLOYMENT mode, you should not use this in production!"
                     + " [code=(IMSCOW0032)]");
        }

        final var trustmanager = getAllTrustingTrustManager();
        final var sslContext = SSLContext.getInstance("SSL");

        sslContext.init(null, trustmanager, new SecureRandom());
        final var sslSocketFactory = sslContext.getSocketFactory();

        okHttpBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustmanager[0]);
        okHttpBuilder.hostnameVerifier((hostname, session) -> true);
    }

    /**
     * Sets the SSLSocketFactory of the ohHttpBuilder.
     *
     * @param manager The KeyStoreManager.
     * @param okHttpBuilder The ohHttpBuilder.
     * @throws NoSuchAlgorithmException Exception thrown.
     * @throws KeyManagementException Exception thrown.
     */
    private static void setSSLSocketFactory(final KeyStoreManager manager,
                                            final OkHttpClient.Builder okHttpBuilder)
            throws NoSuchAlgorithmException, KeyManagementException {
        final var trustManager = manager.getTrustManager();
        final var sslContext = SSLContext.getInstance("TLS");

        sslContext.init(null, new TrustManager[]{trustManager}, null);

        final var sslSocketFactory = sslContext.getSocketFactory();

        okHttpBuilder.sslSocketFactory(sslSocketFactory, trustManager);
    }

    /**
     * Get the Builder for the OkHttpClient.
     *
     * @return The OkHttpClient-Builder.
     */
    @NotNull
    private static OkHttpClient.Builder getOkHttpBuilder() {
        if (log.isDebugEnabled()) {
            log.debug("Creating OkHttp client... [code=(IMSCOD0075)]");
        }
        return new OkHttpClient.Builder();
    }

    /**
     * Get all trusting TrustManager.
     *
     * @return Array of TrustManagers.
     */
    private static TrustManager[] getAllTrustingTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final java.security.cert.X509Certificate[] chain,
                                                   final String authType) {
                    }

                    @Override
                    public void checkServerTrusted(final java.security.cert.X509Certificate[] chain,
                                                   final String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }

    /**
     * Set or update the Client.
     *
     * @param configContainer The Configuration of the Connector.
     * @throws NoSuchAlgorithmException Thrown exception.
     * @throws KeyManagementException Thrown exception.
     */
    private void setClient(final ConfigContainer configContainer)
            throws NoSuchAlgorithmException, KeyManagementException {
        this.client =
                createClientBuilder(configContainer.getConfigurationModel(),
                                    configContainer.getKeyStoreManager())
                        .addInterceptor(new EncodingInterceptor())
                        .build();
    }

    /**
     * Recreate the client builder with a new config
     * (can be called when the configurationmodel or truststore changes).
     *
     * @throws NoSuchAlgorithmException If the cryptographic is
     * unknown when building an {@link OkHttpClient}.
     * @throws KeyManagementException If there is an error with any
     * configured key when building an {@link OkHttpClient}.
     */
    public void updateConfig() throws KeyManagementException, NoSuchAlgorithmException {
        setClient(configContainer);
    }

    /**
     * Request a client with custom timeouts, set a value to set timeout,
     * set null to ignore and use the default value for this timeout.
     *
     * @param connectTimeout Max timeout for connecting to target host (null = default
     *                       values are used).
     * @param readTimeout Max timeout for waiting for the target response (null = default
     *                    values are used).
     * @param writeTimeout Max timeout for sending the response to the target (null = default
     *                     values are used).
     * @param callTimeout Max timeout for the whole http request (null = default values are used).
     * @return An OkHttpClient configured using the current connector configuration and truststore
     * certificates, with the given timeouts set.
     */
    public OkHttpClient getClientWithTimeouts(final Duration connectTimeout,
                                              final Duration readTimeout,
                                              final Duration writeTimeout,
                                              final Duration callTimeout) {

        final var withTimeout =
                rebuildClientWithTimeouts(client, connectTimeout,
                                          readTimeout, writeTimeout,
                                          callTimeout);

        if (log.isDebugEnabled()) {
            log.debug("Ok Http Client Protocols: [code=(IMSCOD0076),"
                      + " protocols=({})]", withTimeout.protocols());
        }
        return withTimeout;
    }

    /**
     * Set custom timeouts for the OkHttpClient and build one.
     *
     * @param client The client which is rebuilt with.
     * @param connectTimeout Max timeout for connecting to target host (null = default values
     *                       are used).
     * @param readTimeout Max timeout for waiting for the target response (null = default
     *                    values are used).
     * @param writeTimeout Max timeout for sending the response to the target (null = default
     *                     values are used).
     * @param callTimeout Max timeout for the whole http request (null = default values are used).
     * @return An OkHttpClient rebuilt with the given timeouts.
     */
    private OkHttpClient rebuildClientWithTimeouts(final OkHttpClient client,
                                                   final Duration connectTimeout,
                                                   final Duration readTimeout,
                                                   final Duration writeTimeout,
                                                   final Duration callTimeout) {
        final var builder = client.newBuilder();

        if (connectTimeout != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting connect timeout: [code=(IMSCOD0077),"
                          + " timeout=({})]", connectTimeout.toString());
            }
            builder.connectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting read timeout: [code=(IMSCOD0078), timeout=({})]",
                          readTimeout.toString());
            }
            builder.readTimeout(readTimeout);
        }
        if (writeTimeout != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting write timeout: [code=(IMSCOD0079), timeout=({})]",
                          writeTimeout.toString());
            }
            builder.writeTimeout(writeTimeout);
        }
        if (callTimeout != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting call timeout. [code=(IMSCOD0080), timeout=({})]",
                          callTimeout.toString());
            }
            builder.callTimeout(callTimeout);
        }

        if (log.isDebugEnabled()) {
            log.debug("Building client. [code=(IMSCOD0081)]");
        }

        final var okHttpClient = builder.build();

        if (log.isDebugEnabled()) {
            log.debug("Ok Http Client Protocols [code=(IMSCOD0082),"
                      + " protocols=({})]", okHttpClient.protocols());
        }

        return okHttpClient;
    }

    /**
     * Adds an interceptor to handle Content-Encodings in response header.
     */
    private class EncodingInterceptor implements Interceptor {
        @Override
        @NotNull
        public Response intercept(final Chain chain) throws IOException {
            if (log.isDebugEnabled()) {
                log.debug("EncodingInterceptor: Checking response encoding for gzip."
                          + " [code=(IMSCOD0144)");
            }

            final var response = chain.proceed(chain.request());

            return handleGzip(response);
        }

        private Response handleGzip(final Response response) {
            final var bodyPresent = (response.body() != null);
            final var gzipPresent = "gzip".equalsIgnoreCase(
                    response.headers().get("Content-Encoding"));

            if (!bodyPresent || !gzipPresent) {
                if (log.isDebugEnabled()) {
                    log.debug("EncodingInterceptor: No body in response or no gzip present."
                              + " [code=(IMSCOD0145)");
                }

                return response;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("EncodingInterceptor: Response body present and"
                              + " gzip encoded, uncompressing. [code=(IMSCOD0146)");
                }

                try {
                    final var responseBody = new GzipSource(response.body().source());
                    final var contentLength = response.body().contentLength();
                    final var strippedHeaders = response.headers()
                                                        .newBuilder()
                                                        .removeAll("Content-Encoding")
                                                        .build();

                    return response.newBuilder()
                                   .headers(strippedHeaders)
                                   .body(new RealResponseBody(
                                           response.body().contentType().toString(),
                                           contentLength,
                                           Okio.buffer(responseBody)))
                                   .build();
                } catch (Exception e) {
                    return response;
                }
            }
        }
    }
}
