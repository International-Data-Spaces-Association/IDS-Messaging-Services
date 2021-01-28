package de.fraunhofer.ids.framework.messaging.protocol.http;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.ClaimsException;
import de.fraunhofer.ids.framework.daps.DapsValidator;
import de.fraunhofer.ids.framework.util.MultipartParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.*;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for sending Http Requests using configuration settings
 */
@Service
public class IdsHttpService implements HttpService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdsHttpService.class);

    private ClientProvider  provider;
    private TimeoutSettings timeoutSettings;
    private DapsValidator   dapsValidator;
    private ConfigContainer configContainer;

    /**
     * @param provider the {@link ClientProvider} used to generate HttpClients with the current connector configuration
     */
    public IdsHttpService( ClientProvider provider, DapsValidator dapsValidator,
                           ConfigContainer configContainer ) {
        this.provider = provider;
        this.dapsValidator = dapsValidator;
        this.configContainer = configContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeouts( Duration connectTimeout, Duration readTimeout, Duration writeTimeout,
                             Duration callTimeout ) {
        this.timeoutSettings = new TimeoutSettings(connectTimeout, readTimeout, writeTimeout, callTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeTimeouts() {
        this.timeoutSettings = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response send( String message, URI target ) throws IOException {
        LOGGER.debug("Creating requestBody");
        var body = RequestBody.create(message, MediaType.parse("application/json"));

        return send(body, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response send( RequestBody requestBody, URI target ) throws IOException {
        LOGGER.debug(String.format("building request to %s", target.toString()));
        Request request = buildRequest(requestBody, target);

        LOGGER.debug(String.format("sending request to %s", target.toString()));
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response sendWithHeaders( RequestBody requestBody, URI target, Map<String, String> headers )
            throws IOException {
        LOGGER.debug(String.format("building request to %s", target.toString()));
        Request request = buildWithHeaders(requestBody, target, headers);

        LOGGER.debug(String.format("sending request to %s", target.toString()));
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response get( URI target ) throws IOException {
        Request request = new Request.Builder().url(target.toString()).get().build();
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response getWithHeaders( URI target, Map<String, String> headers ) throws IOException {
        var builder = new Request.Builder().url(target.toString()).get();

        headers.keySet().forEach(key -> {
            LOGGER.debug(String.format("adding header part (%s,%s)", key, headers.get(key)));
            builder.addHeader(key, headers.get(key));
        });

        var request = builder.build();

        return sendRequest(request, getClientWithSettings());
    }

    /**
     * Build a {@link Request} from given {@link RequestBody} and target {@link URI}
     *
     * @param requestBody {@link RequestBody} object to be sent
     *
     * @return the built http {@link Request}
     */
    private Request buildRequest( RequestBody requestBody, URI target ) {
        var targetURL = target.toString();
        LOGGER.info("URL is valid: " + HttpUrl.parse(targetURL));

        return new Request.Builder()
                .url(targetURL)
                .post(requestBody)
                .build();
    }

    /**
     * Build a {@link Request} from given {@link RequestBody} and target {@link URI},
     * add extra header fields provided in headers map-.
     *
     * @param requestBody {@link RequestBody} object to be sent
     * @param headers     a Map of http headers for the header of the built request
     *
     * @return the build http {@link Request}
     */
    private Request buildWithHeaders( RequestBody requestBody, URI target, Map<String, String> headers ) {
        var targetURL = target.toString();
        LOGGER.info("URL is valid: " + HttpUrl.parse(targetURL));

        //!!! DO NOT PRINT RESPONSE BECAUSE RESPONSE BODY IS JUST ONE TIME READABLE
        // --> Message could not be parsed java.io.IOException: closed
        Request.Builder builder = new Request.Builder()
                .url(targetURL)
                .post(requestBody);

        //add all headers to request
        LOGGER.debug("Adding headers");
        headers.keySet().forEach(key -> {
            LOGGER.debug(String.format("adding header part (%s,%s)", key, headers.get(key)));
            builder.addHeader(key, headers.get(key));
        });

        return builder.build();
    }

    /**
     * Sends a generated request http message to the defined address.
     *
     * @param request POST Request with the message as body
     * @param client  {@link OkHttpClient} for sending Request
     *
     * @return Response object containing the return message from the broker
     *
     * @throws IOException if the request could not be executed due to cancellation, a connectivity problem or timeout.
     */
    private Response sendRequest( Request request, OkHttpClient client ) throws IOException {
        LOGGER.info("Request is HTTPS: " + request.isHttps());

        Response response = client.newCall(request).execute();

        if( !response.isSuccessful() ) {
            LOGGER.error("Error while sending the request!");
            throw new IOException("Unexpected code " + response + " With Body: " + Objects
                    .requireNonNull(response.body()).string());
        }

        return response;
    }

    /**
     * Sends asynchronously a generated request http message to the defined address.
     *
     * @param request  POST Request with the message as body
     * @param client   {@link OkHttpClient} for sending Request
     * @param callback {@link Callback} for response handling
     */
    private void sendAsyncRequest( Request request, OkHttpClient client, Callback callback ) {
        LOGGER.info("Request is HTTPS: " + request.isHttps());

        client.newCall(request).enqueue(callback);

        LOGGER.info("Callback for async request has been enqueued.");
    }

    /**
     * Get an OkHttpClient with the current Timeout Settings.
     *
     * @return client with set timeouts
     */
    private OkHttpClient getClientWithSettings() {
        if( timeoutSettings != null ) {
            LOGGER.debug("Generating a Client with specified timeout settings.");

            return provider.getClientWithTimeouts(
                    timeoutSettings.getConnectTimeout(),
                    timeoutSettings.getReadTimeout(),
                    timeoutSettings.getWriteTimeout(),
                    timeoutSettings.getCallTimeout()
            );
        }

        LOGGER.debug("No timeout settings specified, using default client.");

        return provider.getClient();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> sendAndCheckDat( RequestBody body, URI target )
            throws IOException, FileUploadException, ClaimsException {
        Response response;

        try {
            response = send(body, target);
        } catch( IOException e ) {
            LOGGER.warn("Message could not be sent!");
            throw e;
        }

        return checkDatFromResponse(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> sendWithHeadersAndCheckDat( RequestBody body, URI target, Map<String, String> headers )
            throws IOException, FileUploadException, ClaimsException {
        Response response;

        try {
            response = sendWithHeaders(body, target, headers);
        } catch( IOException e ) {
            LOGGER.warn("Message could not be sent!");
            throw e;
        }

        return checkDatFromResponse(response);
    }

    /**
     * @param response {@link Response} from an IDS Http request
     *
     * @return Multipart Map with header and payload part of response
     *
     * @throws IOException         if request cannot be sent
     * @throws FileUploadException if response cannot be parsed to multipart map
     * @throws ClaimsException     if DAT of response is invalid or cannot be parsed
     */
    private Map<String, String> checkDatFromResponse( Response response )
            throws IOException, ClaimsException, FileUploadException {
        //if connector is set to test deployment: ignore DAT Tokens
        var ignoreDAT =
                configContainer.getConfigModel().getConnectorDeployMode() == ConnectorDeployMode.TEST_DEPLOYMENT;
        var responseString = Objects.requireNonNull(response.body()).string();
        var valid = ignoreDAT || dapsValidator.checkDat(responseString);

        if( !valid ) {
            LOGGER.warn("DAT of incoming response is not valid!");
            throw new ClaimsException("DAT of incoming response is not valid!");
        }
        try {
            return MultipartParser.stringToMultipart(responseString);
        } catch( FileUploadException e ) {
            LOGGER.warn("Could not parse incoming response to multipart map!");
            throw e;
        }
    }

    /**
     * Inner class, managing timeout settings for custom HttpClients
     */
    @AllArgsConstructor
    @Data
    private class TimeoutSettings {
        private Duration connectTimeout;
        private Duration readTimeout;
        private Duration writeTimeout;
        private Duration callTimeout;
    }
}
