package de.fraunhofer.ids.framework.config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.ids.framework.config.ssl.keystore.KeyStoreManager;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The ClientProvider uses the {@link ConfigContainer} to rebuild clients, when a new configurationContainer is created
 */
public class ClientProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClientProvider.class);

    private ConfigContainer configContainer;
    private OkHttpClient    client;

    /**
     * Constructor, creating a Client provider using the KeyStore part from the ConfigurationContainer
     *
     * @param configContainer the {@link ConfigContainer} managing current configurations
     *
     * @throws NoSuchAlgorithmException if the cryptographic is unknown when building an {@link OkHttpClient}
     * @throws KeyManagementException   if there is an error with any configured key when building an {@link OkHttpClient}
     */
    public ClientProvider( ConfigContainer configContainer )
            throws KeyManagementException, NoSuchAlgorithmException {
        this.configContainer = configContainer;
        this.client = createClientBuilder(configContainer.getConfigModel(), configContainer.getKeyManager()).build();
    }

    /**
     * Getter for the current OkHttpClient
     *
     * @return the OkHttpClient using the current configuration
     */
    public OkHttpClient getClient() {
        return client;
    }

    /**
     * recreate the client builder with a new config (can be called when the configurationmodel or truststore changes)
     *
     * @throws NoSuchAlgorithmException if the cryptographic is unknown when building an {@link OkHttpClient}
     * @throws KeyManagementException   if there is an error with any configured key when building an {@link OkHttpClient}
     */
    public void updateConfig() throws KeyManagementException, NoSuchAlgorithmException {
        this.client = createClientBuilder(configContainer.getConfigModel(), configContainer.getKeyManager()).build();
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
    public OkHttpClient getClientWithTimeouts( Duration connectTimeout, Duration readTimeout, Duration writeTimeout,
                                               Duration callTimeout ) {
        LOGGER.debug("Creating OkHttp client");
        var withTimeout = rebuildClientWithTimeouts(client, connectTimeout, readTimeout, writeTimeout, callTimeout);
        LOGGER.info("Ok Http Client Protocols" + withTimeout.protocols());
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
    private OkHttpClient rebuildClientWithTimeouts( OkHttpClient client, Duration connectTimeout, Duration readTimeout,
                                                    Duration writeTimeout, Duration callTimeout ) {
        var builder = client.newBuilder();
        if( connectTimeout != null ) {
            LOGGER.debug(String.format("Setting connect timeout: %s ", connectTimeout.toString()));
            builder.connectTimeout(connectTimeout);
        }
        if( readTimeout != null ) {
            LOGGER.debug(String.format("Setting read timeout: %s ", readTimeout.toString()));
            builder.callTimeout(callTimeout);
        }
        if( writeTimeout != null ) {
            LOGGER.debug(String.format("Setting write timeout: %s ", writeTimeout.toString()));
            builder.writeTimeout(writeTimeout);
        }
        if( callTimeout != null ) {
            LOGGER.debug(String.format("Setting call timeout: %s ", callTimeout.toString()));
            builder.callTimeout(callTimeout);
        }
        LOGGER.debug("Building client!");
        OkHttpClient okHttpClient = builder.build();
        LOGGER.info("Ok Http Client Protocols" + okHttpClient.protocols());
        return okHttpClient;
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
    private static OkHttpClient.Builder createClientBuilder( ConfigurationModel connector, KeyStoreManager manager )
            throws NoSuchAlgorithmException, KeyManagementException {
        LOGGER.debug("Creating OkHttp client");
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        X509TrustManager trustManager = manager.getTrustManager();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{ trustManager }, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        builder.sslSocketFactory(sslSocketFactory, trustManager);
        LOGGER.debug("Created SSLSocketFactory");

        //if the connector has a proxy set
        if( connector.getConnectorProxy() != null ) {
            //if there is any proxy in the proxylist
            var proxyconf = connector.getConnectorProxy().stream().findAny().orElse(null);
            if( proxyconf != null ) {
                LOGGER.debug("Proxy is set active! Configuring Proxy.");
                //create and set Proxy Authenticator with BasicAuth if proxy username and password are set
                if( proxyconf.getProxyAuthentication() != null
                    && proxyconf.getProxyAuthentication().getAuthUsername() != null
                    && proxyconf.getProxyAuthentication().getAuthPassword() != null ) {
                    LOGGER.debug("Setting Proxy Authenticator");
                    Authenticator proxyAuthenticator = ( route, response ) -> {
                        String credential = Credentials.basic(proxyconf.getProxyAuthentication().getAuthUsername(),
                                                              proxyconf.getProxyAuthentication().getAuthPassword());
                        return response.request().newBuilder()
                                       .header("Proxy-Authorization", credential)
                                       .build();
                    };
                    builder.proxyAuthenticator(proxyAuthenticator);
                } else {
                    LOGGER.debug("No Proxy Authentication credentials are set!");
                }
                LOGGER.debug("Create a ProxySelector");
                //create a custom proxySelector (will select the proxy when request goes to host not in NO_PROXY list, and NO_PROXY otherwise)
                final ProxySelector proxySelector = new ProxySelector() {
                    @Override
                    public List<Proxy> select( URI uri ) {
                        //create a List of size 1 containing the possible Proxy
                        final List<Proxy> proxyList = new ArrayList<>(1);
                        if( proxyconf.getNoProxy().contains(uri) ) {
                            LOGGER.debug(String.format("URI %s is in NoProxy List, no proxy is used", uri.toString()));
                            //if the called uri is in the Exceptions of the Connectors ProxyConfiguration use no proxy
                            proxyList.add(Proxy.NO_PROXY);
                        } else {
                            LOGGER.debug(String.format("URI %s is not in NoProxy List, use configured Proxy",
                                                       uri.toString()));
                            //else use proxy with ProxyConfig
                            URI proxyAddress = proxyconf.getProxyURI();
                            String proxyHost = proxyAddress.getHost();
                            int proxyPort = proxyAddress.getPort();
                            LOGGER.info("Address: " + proxyHost + " ,Port: " + proxyPort);
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
}
