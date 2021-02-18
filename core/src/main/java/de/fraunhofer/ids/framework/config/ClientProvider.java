package de.fraunhofer.ids.framework.config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
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
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;


/**
 * The ClientProvider uses the {@link ConfigContainer} to rebuild clients, when a new configurationContainer is created
 */
@Slf4j
public class ClientProvider {

    private ConfigContainer configContainer;
    @Getter
    private OkHttpClient    client;

    /**
     * Constructor, creating a Client provider using the KeyStore part from the ConfigurationContainer
     *
     * @param configContainer the {@link ConfigContainer} managing current configurations
     *
     * @throws NoSuchAlgorithmException if the cryptographic is unknown when building an {@link OkHttpClient}
     * @throws KeyManagementException   if there is an error with any configured key when building an {@link OkHttpClient}
     */
    public ClientProvider( final ConfigContainer configContainer )
            throws KeyManagementException, NoSuchAlgorithmException {
        this.configContainer = configContainer;
        this.client = createClientBuilder(
                configContainer.getConfigurationModel(), configContainer.getKeyStoreManager()
        ).build();
    }

    /**
     * Create the client builder, which can be used to build the OkHttpClient directly, or to customize timeouts for the client
     *
     * @param connector the current connector configuration
     * @param manager   the current key- and truststore
     *
     * @return an {@link okhttp3.OkHttpClient.Builder} using the current configuration of the connector
     *
     * @throws NoSuchAlgorithmException if the cryptographic is unknown
     * @throws KeyManagementException   if there is an error with any configured key
     */
    private static OkHttpClient.Builder createClientBuilder( final ConfigurationModel connector,
                                                             final KeyStoreManager manager )
            throws NoSuchAlgorithmException, KeyManagementException {
        log.debug("Creating OkHttp client");
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if( connector.getConnectorDeployMode() == ConnectorDeployMode.PRODUCTIVE_DEPLOYMENT ) {
            log.debug("Productive Deployment, use Trustmanager vrom KeyStoreManager");
            var trustManager = manager.getTrustManager();
            var sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{ trustManager }, null);
            var sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        } else if( connector.getConnectorDeployMode() == ConnectorDeployMode.TEST_DEPLOYMENT ) {
            log.debug("Test Deployment, use all trusting trustmanager");
            log.warn(
                    "Trustmanager is trusting all Certificates in TEST_DEPLOYMENT mode, you should not use this in production!");
            var trustmanager = getAllTrustingTrustManager();
            var sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustmanager, new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustmanager[0]);
            builder.hostnameVerifier(( hostname, session ) -> true);
        }
        log.debug("Created SSLSocketFactory");
        //if the connector has a proxy set
        if( connector.getConnectorProxy() != null ) {
            //if there is any proxy in the proxylist
            var proxyconf = connector.getConnectorProxy().stream().findAny().orElse(null);
            if( proxyconf != null ) {
                log.debug("Proxy is set active! Configuring Proxy.");
                //create and set Proxy Authenticator with BasicAuth if proxy username and password are set
                if( proxyconf.getProxyAuthentication() != null
                    && proxyconf.getProxyAuthentication().getAuthUsername() != null
                    && proxyconf.getProxyAuthentication().getAuthPassword() != null ) {
                    log.debug("Setting Proxy Authenticator");
                    Authenticator proxyAuthenticator = ( route, response ) -> {
                        String credential = Credentials.basic(proxyconf.getProxyAuthentication().getAuthUsername(),
                                                              proxyconf.getProxyAuthentication().getAuthPassword());
                        return response.request().newBuilder()
                                       .header("Proxy-Authorization", credential)
                                       .build();
                    };
                    builder.proxyAuthenticator(proxyAuthenticator);
                } else {
                    log.debug("No Proxy Authentication credentials are set!");
                }
                log.debug("Create a ProxySelector");
                //create a custom proxySelector (will select the proxy when request goes to host not in NO_PROXY list, and NO_PROXY otherwise)
                final ProxySelector proxySelector = new ProxySelector() {
                    @Override
                    public List<Proxy> select( URI uri ) {
                        //create a List of size 1 containing the possible Proxy
                        final List<Proxy> proxyList = new ArrayList<>(1);
                        if( proxyconf.getNoProxy().contains(uri) ) {
                            log.debug(String.format("URI %s is in NoProxy List, no proxy is used", uri.toString()));
                            //if the called uri is in the Exceptions of the Connectors ProxyConfiguration use no proxy
                            proxyList.add(Proxy.NO_PROXY);
                        } else {
                            log.debug(String.format("URI %s is not in NoProxy List, use configured Proxy",
                                                    uri.toString()));
                            //else use proxy with ProxyConfig
                            URI proxyAddress = proxyconf.getProxyURI();
                            String proxyHost = proxyAddress.getHost();
                            int proxyPort = proxyAddress.getPort();
                            log.info("Address: " + proxyHost + " ,Port: " + proxyPort);
                            proxyList.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
                        }
                        return proxyList;
                    }

                    @Override
                    public void connectFailed( URI uri, SocketAddress sa, IOException ioe ) {
                        throw new UnsupportedOperationException("The selected Proxy is unavailable!");
                    }
                };
                //set proxySelector for the okhttpclient
                builder.proxySelector(proxySelector);
            }
        }
        return builder;
    }

    private static TrustManager[] getAllTrustingTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted( java.security.cert.X509Certificate[] chain, String authType ) {
                    }

                    @Override
                    public void checkServerTrusted( java.security.cert.X509Certificate[] chain, String authType ) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }

    /**
     * recreate the client builder with a new config (can be called when the configurationmodel or truststore changes)
     *
     * @throws NoSuchAlgorithmException if the cryptographic is unknown when building an {@link OkHttpClient}
     * @throws KeyManagementException   if there is an error with any configured key when building an {@link OkHttpClient}
     */
    public void updateConfig() throws KeyManagementException, NoSuchAlgorithmException {
        this.client = createClientBuilder(configContainer.getConfigurationModel(), configContainer.getKeyStoreManager())
                .build();
    }

    /**
     * Request a client with custom timeouts, set a value to set timeout, set null to ignore and use the default value for this timeout
     *
     * @param connectTimeout max timeout for connecting to target host (null = default values are used)
     * @param readTimeout    max timeout for waiting for the target response (null = default values are used)
     * @param writeTimeout   max timeout for sending the response to the target (null = default values are used)
     * @param callTimeout    max timeout for the whole http request (null = default values are used)
     *
     * @return an OkHttpClient configured using the current connector configuration and truststore certificates, with the given timeouts set
     */
    public OkHttpClient getClientWithTimeouts( final Duration connectTimeout,
                                               final Duration readTimeout,
                                               final Duration writeTimeout,
                                               final Duration callTimeout ) {
        log.debug("Creating OkHttp client");
        var withTimeout = rebuildClientWithTimeouts(client, connectTimeout, readTimeout, writeTimeout, callTimeout);

        log.info("Ok Http Client Protocols" + withTimeout.protocols());
        return withTimeout;
    }

    /**
     * Set custom timeouts for the OkHttpClient and build one
     *
     * @param client         the client which is rebuilt with
     * @param connectTimeout max timeout for connecting to target host (null = default values are used)
     * @param readTimeout    max timeout for waiting for the target response (null = default values are used)
     * @param writeTimeout   max timeout for sending the response to the target (null = default values are used)
     * @param callTimeout    max timeout for the whole http request (null = default values are used)
     *
     * @return an OkHttpClient rebuilt with the given timeouts
     */
    private OkHttpClient rebuildClientWithTimeouts( final OkHttpClient client,
                                                    final Duration connectTimeout,
                                                    final Duration readTimeout,
                                                    final Duration writeTimeout,
                                                    final Duration callTimeout ) {
        var builder = client.newBuilder();
        if( connectTimeout != null ) {
            log.debug(String.format("Setting connect timeout: %s ", connectTimeout.toString()));
            builder.connectTimeout(connectTimeout);
        }
        if( readTimeout != null ) {
            log.debug(String.format("Setting read timeout: %s ", readTimeout.toString()));
            builder.readTimeout(readTimeout);
        }
        if( writeTimeout != null ) {
            log.debug(String.format("Setting write timeout: %s ", writeTimeout.toString()));
            builder.writeTimeout(writeTimeout);
        }
        if( callTimeout != null ) {
            log.debug(String.format("Setting call timeout: %s ", callTimeout.toString()));
            builder.callTimeout(callTimeout);
        }
        log.debug("Building client!");
        OkHttpClient okHttpClient = builder.build();

        log.info("Ok Http Client Protocols" + okHttpClient.protocols());
        return okHttpClient;
    }
}
