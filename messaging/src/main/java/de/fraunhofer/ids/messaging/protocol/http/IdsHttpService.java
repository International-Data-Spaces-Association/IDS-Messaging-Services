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
package de.fraunhofer.ids.messaging.protocol.http;

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
import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.core.config.ClientProvider;
import de.fraunhofer.ids.messaging.core.config.ConfigContainer;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsValidator;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartDatapart;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.jena.riot.RiotException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending Http Requests using configuration settings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class IdsHttpService implements HttpService {
    ClientProvider provider;
    DapsValidator dapsValidator;
    ConfigContainer configContainer;
    Serializer serializer;

    @NonFinal
    private TimeoutSettings timeoutSettings;

    @NonFinal
    @Value("#{new Boolean('${shacl.validation:false}')}")
    private Boolean shaclValidation;

    /**
     * @param response {@link Response} from an IDS Http request
     *
     * @return Multipart Map with header and payload part of response
     *
     * @throws IOException     if request cannot be sent
     * @throws ClaimsException if DAT of response is invalid or cannot be parsed
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
            if (log.isInfoEnabled()) {
                log.info(messageString);
            }

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
                log.info("Received response passed SHACL Validation.");
            }
        }

        final Map<String, Object> extraAttributes = new ConcurrentHashMap<>();

        try {
            final var message = serializer.deserialize(messageString, Message.class);
            final var payloadString = multipartResponse.get(MultipartDatapart.PAYLOAD.toString());

            if (payloadString != null) {
                try {
                    final var connector = serializer.deserialize(payloadString, Connector.class);

                    if (message.getIssuerConnector().equals(connector.getId())) {
                        extraAttributes.put("securityProfile",
                                            connector
                                                .getSecurityProfile()
                                                .getId());
                    }

                } catch (IOException | RiotException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not deserialize Playload " + e.getMessage());
                        log.warn("Skipping Connector-SecurityProfile Attribute!");
                    }
                }
            }

            final var ignoreDAT = configContainer
                                    .getConfigurationModel()
                                    .getConnectorDeployMode()
                                        == ConnectorDeployMode.TEST_DEPLOYMENT;
            var valid = true;

            if (!ignoreDAT && !(message instanceof RejectionMessage)) {
                valid = dapsValidator.checkDat(message.getSecurityToken(), extraAttributes);
            }

            if (!valid) {
                if (log.isWarnEnabled()) {
                    log.warn("DAT of incoming response is not valid!");
                }

                throw new ClaimsException("DAT of incoming response is not valid!");
            }

            return multipartResponse;
        } catch (IOException ioException) {
            //serializer.deserialize messageString thre IOException, mapping to DeserializeException
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
            log.debug("Creating requestBody");
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
        if (log.isDebugEnabled()) {
            log.debug(String.format("building request to %s", target.toString()));
        }

        final var request = buildRequest(requestBody, target);

        if (log.isDebugEnabled()) {
            log.debug(String.format("sending request to %s", target));
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

        if (log.isDebugEnabled()) {
            log.debug(String.format("building request to %s", target.toString()));
        }

        final var request = buildWithHeaders(requestBody, target, headers);

        if (log.isDebugEnabled()) {
            log.debug(String.format("sending request to %s", target));
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
                 log.debug(String.format("adding header part (%s,%s)",
                                         key, headers.get(key)));
             }

             builder.addHeader(key, headers.get(key));
        });

        final var request = builder.build();

        return sendRequest(request, getClientWithSettings());
    }

    /**
     * Build a {@link Request} from given {@link RequestBody} and target {@link URI}.
     *
     * @param requestBody {@link RequestBody} object to be sent
     * @param target      The target-URI of the request
     *
     * @return the built http {@link Request}
     */
    private Request buildRequest(final RequestBody requestBody, final URI target) {
        final var targetURL = target.toString();

        if (log.isInfoEnabled()) {
            log.info("URL is valid: " + HttpUrl.parse(targetURL));
        }

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
     * @param target      The target-URI of the request
     * @param headers     a Map of http headers for the header of the built request
     *
     * @return the build http {@link Request}
     */
    private Request buildWithHeaders(final RequestBody requestBody,
                                     final URI target,
                                     final Map<String, String> headers) {
        final var targetURL = target.toString();

        if (log.isInfoEnabled()) {
            log.info("URL is valid: " + HttpUrl.parse(targetURL));
        }

        //!!! DO NOT PRINT RESPONSE BECAUSE RESPONSE BODY IS JUST ONE TIME READABLE
        // --> Message could not be parsed java.io.IOException: closed
        final var builder = new Request.Builder()
                .url(targetURL)
                .post(requestBody);

        //add all headers to request
        if (log.isDebugEnabled()) {
            log.debug("Adding headers");
        }

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
     * @return Response object containing the return message from the broker
     * @throws IOException if the request could not be executed due
     * to cancellation, a connectivity problem or timeout.
     */
    private Response sendRequest(final Request request,
                                 final OkHttpClient client) throws IOException {
        if (log.isInfoEnabled()) {
            log.info("Request is HTTPS: " + request.isHttps());
        }

        final var response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            if (log.isErrorEnabled()) {
                log.error("Error while sending the request!");
            }

            throw new IOException("Unexpected code " + response + " With Body: " + Objects
                    .requireNonNull(response.body()).string());
        }

        return response;
    }

    /**
     * Get an OkHttpClient with the current Timeout Settings.
     *
     * @return client with set timeouts
     */
    private OkHttpClient getClientWithSettings() {
        OkHttpClient client;

        if (timeoutSettings == null) {
            if (log.isDebugEnabled()) {
                log.debug("No timeout settings specified, using default client.");
            }

            client = provider.getClient();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Generating a Client with specified timeout settings.");
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
            if (log.isErrorEnabled()) {
                log.error("Message could not be sent! " + ioException.getMessage());
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
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Message could not be sent!");
            }

            throw e;
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
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Message could not be sent!");
            }

            throw e;
        }

        return checkDatFromResponse(response);
    }

    /**
     * Inner class, managing timeout settings for custom HttpClients.
     */
    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private class TimeoutSettings {
        Duration connectTimeout;
        Duration readTimeout;
        Duration writeTimeout;
        Duration callTimeout;
    }
}
