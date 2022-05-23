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

import ids.messaging.common.DeserializeException;
import ids.messaging.core.daps.ClaimsException;
import ids.messaging.protocol.multipart.parser.MultipartParseException;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Interface for the HttpService.
 */
public interface HttpService {
    /**
     * Set the timeouts for the OkHttpClient.
     *
     * @param connectTimeout Max timeout for connecting to target host (null = default values
     *                       are used).
     * @param readTimeout Max timeout for waiting for the target response (null = default
     *                    values are used).
     * @param writeTimeout Max timeout for sending the response to the target (null = default
     *                     values are used).
     * @param callTimeout Max timeout for the whole http request (null = default values are used).
     */
    void setTimeouts(Duration connectTimeout, Duration readTimeout,
                     Duration writeTimeout, Duration callTimeout);

    /**
     * Reset client timeouts to OkHttp default values.
     */
    void removeTimeouts();

    /**
     * Sends plaintext message as http(s) request to the defined target.
     *
     * @param target The target host of the request.
     * @param message IDS-Message to be sent (as JSON).
     * @return True if the message was successfully sent, else false.
     * @throws IOException If the request could not be executed due to cancellation,
     * a connectivity problem or timeout.
     */
    Response send(String message, URI target) throws IOException;

    /**
     * Sends plaintext message as http(s) request to the defined target.
     *
     * @param request The {@link Request} to be send.
     * @return The HttpResponse that comes back for the sent Message.
     * @throws IOException If the request could not be executed due to cancellation, a
     * connectivity problem or timeout.
     */
    Response send(Request request) throws IOException;

    /**
     * Sends a given requestBody as http(s) request to the defined in address.
     *
     * @param target The target host of the request.
     * @param requestBody {@link RequestBody} object to be sent.
     * @return The HttpResponse that comes back for the sent Message.
     * @throws IOException If the request could not be executed due to cancellation, a connectivity
     * problem or timeout.
     */
    Response send(RequestBody requestBody, URI target) throws IOException;

    /**
     * Sends a given requestBody as http(s) request to the defined in address,
     * extra fields for the header can be provided in headers map.
     *
     * @param requestBody {@link RequestBody} object to be sent.
     * @param headers A Map of http headers for the header of the built request.
     * @param target The target host of the request.
     * @return The HttpResponse that comes back for the sent Message.
     * @throws IOException If the request could not be executed due to cancellation,
     * a connectivity problem or timeout.
     */
    Response sendWithHeaders(RequestBody requestBody, URI target,
                             Map<String, String> headers) throws IOException;

    /**
     * Sends a http GET request to the target.
     *
     * @param target The target host of the request.
     * @return The HttpResponse from the get request.
     * @throws IOException If the request could not be executed due to cancellation, a connectivity
     * problem or timeout.
     */
    Response get(URI target) throws IOException;

    /**
     * Sends a http GET request to the target, extra fields for the header can be
     * provided in headers map.
     *
     * @param target The target host of the request.
     * @param headers A Map of http headers for the header of the built request.
     * @return The HttpResponse from the get request.
     * @throws IOException If the request could not be executed due to cancellation, a
     * connectivity problem or timeout.
     */
    Response getWithHeaders(URI target, Map<String, String> headers) throws IOException;

    /**
     * Sends a request and validates the DAT of the response.
     *
     * @param request To be sent.
     * @return Multipart Map with header and payload part of response.
     * @throws IOException If request cannot be sent.
     * @throws ClaimsException If response cannot be parsed to multipart map.
     * @throws MultipartParseException If DAT of response is invalid or cannot be parsed.
     * @throws ClaimsException Exception while validating the DAT from the Broker Response.
     * @throws DeserializeException Exception that is thrown if deserializing a message threw
     * an IOException.
     * @throws ShaclValidatorException SHACL-Validation, received message header does not conform
     * to IDS-Infomodel and did not pass SHACL-Validation.
     * @throws SendMessageException Sending the IDS-Request returns an IOException.
     */
    Map<String, String> sendAndCheckDat(Request request)
            throws
            IOException,
            ClaimsException,
            MultipartParseException,
            SendMessageException,
            ShaclValidatorException,
            DeserializeException;

    /**
     * Sends a request to a given target and validates the DAT of the response.
     *
     * @param body RequestBody to be sent.
     * @param target The target to send the message to.
     * @return Multipart Map with header and payload part of response.
     * @throws IOException If request cannot be sent.
     * @throws MultipartParseException If response cannot be parsed to multipart map.
     * @throws ClaimsException If DAT of response is invalid or cannot be parsed.
     * @throws ClaimsException Exception while validating the DAT from the Broker Response.
     * @throws DeserializeException exception that is thrown if deserializing a message threw
     * an IOException.
     * @throws ShaclValidatorException SHACL-Validation, received message header does not conform
     * to IDS-Infomodel and did not pass SHACL-Validation.
     */
    Map<String, String> sendAndCheckDat(RequestBody body, URI target)
            throws
            IOException,
            MultipartParseException,
            ClaimsException,
            DeserializeException,
            ShaclValidatorException;

    /**
     * @param body RequestBody to be sent.
     * @param target TargetURI of the request.
     * @param headers Additional headers for the Http header.
     * @return Multipart Map with header and payload part of response.
     * @throws IOException If request cannot be sent.
     * @throws MultipartParseException If response cannot be parsed to multipart map.
     * @throws ClaimsException If DAT of response is invalid or cannot be parsed.
     * @throws ClaimsException Exception while validating the DAT from the Broker Response.
     * @throws DeserializeException Exception that is thrown if deserializing a message threw
     * an IOException.
     * @throws ShaclValidatorException SHACL-Validation, received message header does not conform
     * to IDS-Infomodel and did not pass SHACL-Validation.
     */
    Map<String, String> sendWithHeadersAndCheckDat(RequestBody body,
                                                   URI target,
                                                   Map<String, String> headers)
            throws
            IOException,
            MultipartParseException,
            ClaimsException,
            DeserializeException,
            ShaclValidatorException;
}
