package de.fraunhofer.ids.framework.messaging.protocol.http;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import de.fraunhofer.ids.framework.daps.ClaimsException;
import de.fraunhofer.ids.framework.daps.DapsValidator;
import de.fraunhofer.ids.framework.util.MultipartDatapart;
import de.fraunhofer.ids.framework.util.MultipartParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.fileupload.FileUploadException;
import org.springframework.stereotype.Service;

/**
 * Service for sending Http Requests using configuration settings
 */
@Slf4j
@Service
public class IdsHttpService implements HttpService {
    private ClientProvider  provider;
    private TimeoutSettings timeoutSettings;
    private DapsValidator   dapsValidator;
    private ConfigContainer configContainer;
    private Serializer serializer;

    /**
     * @param provider the {@link ClientProvider} used to generate HttpClients with the current connector configuration
     */
    public IdsHttpService( ClientProvider provider, DapsValidator dapsValidator,
                           ConfigContainer configContainer, Serializer serializer ) {
        this.provider = provider;
        this.dapsValidator = dapsValidator;
        this.configContainer = configContainer;
        this.serializer = serializer;
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
        log.debug("Creating requestBody");
        var body = RequestBody.create(message, MediaType.parse("application/json"));

        return send(body, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response send( Request request ) throws IOException {
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response send( RequestBody requestBody, URI target ) throws IOException {
        log.debug(String.format("building request to %s", target.toString()));
        var request = buildRequest(requestBody, target);

        log.debug(String.format("sending request to %s", target.toString()));
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response sendWithHeaders( RequestBody requestBody, URI target, Map<String, String> headers )
            throws IOException {
        log.debug(String.format("building request to %s", target.toString()));
        var request = buildWithHeaders(requestBody, target, headers);

        log.debug(String.format("sending request to %s", target.toString()));
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response get( URI target ) throws IOException {
        var request = new Request.Builder().url(target.toString()).get().build();
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response getWithHeaders( URI target, Map<String, String> headers ) throws IOException {
        var builder = new Request.Builder().url(target.toString()).get();

        headers.keySet().forEach(key -> {
            log.debug(String.format("adding header part (%s,%s)", key, headers.get(key)));
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
        log.info("URL is valid: " + HttpUrl.parse(targetURL));

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
        log.info("URL is valid: " + HttpUrl.parse(targetURL));

        //!!! DO NOT PRINT RESPONSE BECAUSE RESPONSE BODY IS JUST ONE TIME READABLE
        // --> Message could not be parsed java.io.IOException: closed
        Request.Builder builder = new Request.Builder()
                .url(targetURL)
                .post(requestBody);

        //add all headers to request
        log.debug("Adding headers");
        headers.keySet().forEach(key -> {
            log.debug(String.format("adding header part (%s,%s)", key, headers.get(key)));
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
        log.info("Request is HTTPS: " + request.isHttps());

        var response = client.newCall(request).execute();

        if( !response.isSuccessful() ) {
            log.error("Error while sending the request!");
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
        log.info("Request is HTTPS: " + request.isHttps());

        client.newCall(request).enqueue(callback);

        log.info("Callback for async request has been enqueued.");
    }

    /**
     * Get an OkHttpClient with the current Timeout Settings.
     *
     * @return client with set timeouts
     */
    private OkHttpClient getClientWithSettings() {
        if( timeoutSettings != null ) {
            log.debug("Generating a Client with specified timeout settings.");

            return provider.getClientWithTimeouts(
                    timeoutSettings.getConnectTimeout(),
                    timeoutSettings.getReadTimeout(),
                    timeoutSettings.getWriteTimeout(),
                    timeoutSettings.getCallTimeout()
            );
        }

        log.debug("No timeout settings specified, using default client.");

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
            log.warn("Message could not be sent!");
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
            log.warn("Message could not be sent!");
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
                configContainer.getConfigurationModel().getConnectorDeployMode() == ConnectorDeployMode.TEST_DEPLOYMENT;
        var responseString = Objects.requireNonNull(response.body()).string();
        var multipartResponse = MultipartParser.stringToMultipart(responseString);
        var messageJson = multipartResponse.get(MultipartDatapart.HEADER.toString());
        var message = serializer.deserialize(messageJson, Message.class);
        var valid = true;
        if(!ignoreDAT && !(message instanceof RejectionMessage)){
            valid = dapsValidator.checkDat(message.getSecurityToken());
        }
        if( !valid ) {
            log.warn("DAT of incoming response is not valid!");
            throw new ClaimsException("DAT of incoming response is not valid!");
        }
        try {
            return MultipartParser.stringToMultipart(responseString);
        } catch( FileUploadException e ) {
            log.warn("Could not parse incoming response to multipart map!");
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
