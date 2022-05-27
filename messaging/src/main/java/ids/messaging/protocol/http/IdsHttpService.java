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
package ids.messaging.protocol.http;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.ids.component.interaction.validation.ShaclValidator;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.DeserializeException;
import ids.messaging.core.config.ClientProvider;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.core.daps.DapsValidator;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import ids.messaging.protocol.multipart.parser.MultipartParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending Http Requests using configuration settings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdsHttpService implements HttpService {

    /**
     * The ClientProvider.
     */
    private final ClientProvider provider;

    /**
     * The DapsValidator.
     */
    private final DapsValidator dapsValidator;

    /**
     * The ConfigContainer.
     */
    private final ConfigContainer configContainer;

    /**
     * The infomodel serializer.
     */
    private final Serializer serializer;

    /**
     * TimeoutSettings for the HttpClient.
     */
    private TimeoutSettings timeoutSettings;

    /**
     * Used to switch SHACL validation off or on.
     */
    @Value("#{new Boolean('${shacl.validation:false}')}")
    private Boolean shaclValidation;

    /**
     * Used to switch logging incoming responses off or on (default off).
     */
    @Value("#{new Boolean('${messaging.log.incoming:false}')}")
    private Boolean logResponses;

    /**
     * Used to switch logging sending requests off or on (default off).
     */
    @Value("#{new Boolean('${messaging.log.outgoing:false}')}")
    private Boolean logRequests;

    /**
     * @param response {@link Response} from an IDS Http request.
     * @return Multipart Map with header and payload part of response.
     * @throws IOException If request cannot be sent.
     * @throws ClaimsException If DAT of response is invalid or cannot be parsed.
     */
    private Map<String, String> checkDatFromResponse(final Response response)
            throws
            MultipartParseException,
            ClaimsException,
            ShaclValidatorException,
            DeserializeException,
            IOException {
        //if connector is set to test deployment: ignore DAT Tokens
        final var responseString = Objects.requireNonNull(response.body()).string();
        final var multipartResponse = MultipartParser.stringToMultipart(responseString);
        final var messageString = multipartResponse.get(MultipartDatapart.HEADER.toString());

        if (Boolean.TRUE.equals(shaclValidation)) {
            try {
                //If the validation is not successful, then this throws an IOException
                ShaclValidator.validateRdf(messageString).conforms();
            } catch (IOException ioException) {
                //catch IOException and throw ShaclValidatorException instead
                throw new ShaclValidatorException("Received message header"
                  + " does not conform to IDS-Infomodel!"
                  + " Received message did not pass SHACL-Validation!");
            }

            if (log.isInfoEnabled()) {
                log.info("Successfully passed SHACL-Validation. [code=(IMSMEI0064)]");
            }
        }

        final Map<String, Object> extraAttributes = new ConcurrentHashMap<>();

        try {
            final var message = serializer.deserialize(messageString, Message.class);
            final var payloadString = multipartResponse.get(MultipartDatapart.PAYLOAD.toString());

            if (isJsonSecProfile(payloadString)) {
                try {
                    final var connector = serializer.deserialize(payloadString, Connector.class);

                    if (message.getIssuerConnector().equals(connector.getId())) {
                        extraAttributes.put("securityProfile",
                                            connector.getSecurityProfile().getId());
                    }
                } catch (Exception e) {
                    //At this point, all exceptions can be caught regardless of their cause.
                    if (log.isDebugEnabled()) {
                        log.debug("Could not deserialize Payload to Connector class."
                                  + " Skipping Connector-SecurityProfile attribute"
                                  + " in DAT validation. [code=(IMSMED0125), exception=({})]",
                                  e.getMessage());
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Payload is no valid JSON or does not contain a securityProfile"
                              + " attribute. Skipping Connector-SecurityProfile attribute"
                              + " in DAT validation. [code=(IMSMED0126)]");
                }
            }

            final var ignoreDAT = configContainer.getConfigurationModel().getConnectorDeployMode()
                                        == ConnectorDeployMode.TEST_DEPLOYMENT;
            var valid = true;

            if (!ignoreDAT && !(message instanceof RejectionMessage)) {
                valid = dapsValidator.checkDat(message.getSecurityToken(), extraAttributes);
            }

            if (!valid) {
                if (log.isWarnEnabled()) {
                    log.warn("DAT of incoming response is not valid! [code=(IMSMEW0045)]");
                }

                throw new ClaimsException("DAT of incoming response is not valid!");
            }

            return multipartResponse;
        } catch (IOException ioException) {
            //serializer.deserialize messageString threw IOException, mapping to
            //DeserializeException
            throw new DeserializeException(ioException);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeouts(final Duration connectTimeout,
                            final Duration readTimeout,
                            final Duration writeTimeout,
                            final Duration callTimeout) {
        this.timeoutSettings =
                new TimeoutSettings(connectTimeout,
                                    readTimeout,
                                    writeTimeout,
                                    callTimeout);
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
    public Response send(final String message, final URI target) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Creating requestBody... [code=(IMSMED0127)]");
        }

        final var body = RequestBody.create(message, MediaType.parse("application/json"));

        return send(body, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response send(final Request request) throws IOException {
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response send(final RequestBody requestBody, final URI target) throws IOException {
        final var request = buildRequest(requestBody, target);

        if (log.isDebugEnabled()) {
            log.debug("Sending request. [code=(IMSMED0128), url=({})]", target);
        }

        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response sendWithHeaders(final RequestBody requestBody,
                                    final URI target,
                                    final Map<String, String> headers)
            throws IOException {
        final var request = buildWithHeaders(requestBody, target, headers);

        if (log.isDebugEnabled()) {
            log.debug("Sending request. [code=(IMSMED0129), url=({})]", target);
        }

        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response get(final URI target) throws IOException {
        final var request = new Request.Builder().url(target.toString()).get().build();
        return sendRequest(request, getClientWithSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response getWithHeaders(final URI target,
                                   final Map<String, String> headers)
            throws IOException {
        final var builder = new Request.Builder().url(target.toString()).get();

        headers.keySet().forEach(key -> {
             if (log.isDebugEnabled()) {
                 log.debug("Adding header. [code=(IMSMED0130), key=({}), header=({})]", key,
                           headers.get(key));
             }

             builder.addHeader(key, headers.get(key));
        });

        final var request = builder.build();

        return sendRequest(request, getClientWithSettings());
    }

    /**
     * Checks if the payload is a valid JSON (array or object) and if it contains
     * a securityProfile specification.
     *
     * @param payload The received payload.
     * @return True if valid JSON and contains securityProfile, else false.
     */
    private boolean isJsonSecProfile(final String payload) {
        if (payload == null || !payload.contains("securityProfile")) {
            return false;
        }

        try {
            new JSONObject(payload);
        } catch (Exception noObject) {
            try {
                new JSONArray(payload);
            } catch (Exception noArray) {
                return false;
            }
        }
        return true;
    }

    /**
     * Build a {@link Request} from given {@link RequestBody} and target {@link URI}.
     *
     * @param requestBody {@link RequestBody} object to be sent.
     * @param target The target-URI of the request.
     * @return The built http {@link Request}.
     */
    private Request buildRequest(final RequestBody requestBody, final URI target) {
        final var targetURL = target.toString();

        if (log.isDebugEnabled()) {
            log.debug("Request URL: [code=(IMSMED0131), url=({})]", HttpUrl.parse(targetURL));
        }

        return new Request.Builder()
                .url(targetURL)
                .post(requestBody)
                .build();
    }

    /**
     * Build a {@link Request} from given {@link RequestBody} and target {@link URI},
     * add extra header fields provided in headers map.
     *
     * @param requestBody {@link RequestBody} object to be sent.
     * @param target The target-URI of the request.
     * @param headers A Map of http headers for the header of the built request.
     * @return The build http {@link Request}.
     */
    private Request buildWithHeaders(final RequestBody requestBody,
                                     final URI target,
                                     final Map<String, String> headers) {
        final var targetURL = target.toString();

        if (log.isDebugEnabled()) {
            log.debug("Request URL: [code=(IMSMED0132), url=({})]", HttpUrl.parse(targetURL));
        }

        //!!! DO NOT PRINT RESPONSE BECAUSE RESPONSE BODY IS JUST ONE TIME READABLE
        // --> Message could not be parsed java.io.IOException: closed
        final var builder = new Request.Builder()
                .url(targetURL)
                .post(requestBody);

        //add all headers to request
        if (log.isDebugEnabled()) {
            log.debug("Adding headers... [code=(IMSMED0133)]");
        }

        headers.keySet().forEach(key -> {
            if (log.isDebugEnabled()) {
                log.debug("Adding header [code=(IMSMED0134), key=({}), header=({})]", key,
                          headers.get(key));
            }
            builder.addHeader(key, headers.get(key));
        });

        return builder.build();
    }

    /**
     * Sends a generated request http message to the defined address.
     *
     * @param request POST Request with the message as body.
     * @param client {@link OkHttpClient} for sending Request.
     * @return Response object containing the return message.
     * @throws IOException If the request could not be executed due to cancellation, a connectivity
     * problem or timeout etc.
     */
    private Response sendRequest(final Request request,
                                 final OkHttpClient client) throws IOException {
        if (logRequests && request.body() != null) {
            try {
                logRequest(request);
            } catch (Exception exception) {
                //Nothing to do, request message could not be logged.
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Sending request to {} ... [code=(IMSMEI0065)]", request.url());
        }

        var response = client.newCall(request).execute();
        final var responseBody = response.body();

        if (Boolean.TRUE.equals(logResponses) && responseBody != null) {
            final var bodyString = responseBody.string();
            log.info("Incoming response body: {} [code=(IMSMEI0066)]", bodyString);
            final var body = ResponseBody.create(bodyString, responseBody.contentType());
            response = response.newBuilder().body(body).build();
        }

        if (!response.isSuccessful()) {
            if (log.isWarnEnabled()) {
                log.warn("Received response but response-code not in 200-299."
                          + " [code=(IMSMEW0046), response-code=({})]", response.code());
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("Successfully received response to request. [code=(IMSMEI0067)]");
            }
        }

        return response;
    }

    /**
     * Logs the request message to be send in log.
     *
     * @param request The request message to be send.
     * @throws IOException If Buffer could not be accessed.
     */
    private void logRequest(final Request request) throws IOException {
        final var requestCopy = request.newBuilder().build();
        final var buffer = new Buffer();
        requestCopy.body().writeTo(buffer);
        log.info("Sending request message: {} [code=(IMSMEI0068)]", buffer.readUtf8());
    }

    /**
     * Get an OkHttpClient with the current Timeout Settings.
     *
     * @return Client with set timeouts.
     */
    private OkHttpClient getClientWithSettings() {
        OkHttpClient client;

        if (timeoutSettings == null) {
            if (log.isDebugEnabled()) {
                log.debug("No timeout settings specified, using default client."
                          + " [code=(IMSMED0136)]");
            }

            client = provider.getClient();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Generating a Client with specified timeout settings."
                          + " [code=(IMSMED0137)]");
            }

            client = provider.getClientWithTimeouts(
                    timeoutSettings.getConnectTimeout(),
                    timeoutSettings.getReadTimeout(),
                    timeoutSettings.getWriteTimeout(),
                    timeoutSettings.getCallTimeout()
            );
        }
        return client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> sendAndCheckDat(final Request request)
            throws
            ClaimsException,
            MultipartParseException,
            SendMessageException,
            ShaclValidatorException,
            DeserializeException,
            IOException {
        Response response;

        try {
            response = send(request);
        } catch (IOException ioException) {
            if (log.isDebugEnabled()) {
                log.debug("Error during transmission of the message! [code=(IMSMED0138),"
                          + " exception=({})]", ioException.getMessage());
            }

            //throw SendMessageException instead of IOException
            throw new SendMessageException(ioException);
        }

        return checkDatFromResponse(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> sendAndCheckDat(final RequestBody body, final URI target)
            throws
            IOException,
            ClaimsException,
            MultipartParseException,
            DeserializeException,
            ShaclValidatorException {
        Response response;

        try {
            response = send(body, target);
        } catch (IOException ioException) {
            if (log.isDebugEnabled()) {
                log.debug("Error during transmission of the message! [code=(IMSMED0139),"
                          + " exception=({})]", ioException.getMessage());
            }

            throw ioException;
        }

        return checkDatFromResponse(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> sendWithHeadersAndCheckDat(final RequestBody body,
                                                          final URI target,
                                                          final Map<String, String> headers)
            throws
            IOException,
            ClaimsException,
            MultipartParseException,
            DeserializeException,
            ShaclValidatorException {
        Response response;

        try {
            response = sendWithHeaders(body, target, headers);
        } catch (IOException ioException) {
            if (log.isDebugEnabled()) {
                log.debug("Error during transmission of the message! [code=(IMSMED0140),"
                          + " exception=({})]", ioException.getMessage());
            }

            throw ioException;
        }

        return checkDatFromResponse(response);
    }

    /**
     * Inner class, managing timeout settings for custom HttpClients.
     */
    @Getter
    @AllArgsConstructor
    private class TimeoutSettings {
        /**
         * Sets the connect timeout for the HttpClient.
         */
        private Duration connectTimeout;

        /**
         * Sets the read timeout for the HttpClient.
         */
        private Duration readTimeout;

        /**
         * Sets the write timeout for the HttpClient.
         */
        private Duration writeTimeout;

        /**
         * Sets the call timeout for the HttpClient.
         */
        private Duration callTimeout;
    }
}
