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
package de.fraunhofer.ids.messaging.requests.builder;

import de.fraunhofer.ids.messaging.common.DeserializeException;
import de.fraunhofer.ids.messaging.common.SerializeException;
import de.fraunhofer.ids.messaging.core.daps.ClaimsException;
import de.fraunhofer.ids.messaging.core.daps.DapsTokenManagerException;
import de.fraunhofer.ids.messaging.protocol.MessageService;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.http.ShaclValidatorException;
import de.fraunhofer.ids.messaging.protocol.multipart.UnknownResponseException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.MessageContainer;
import de.fraunhofer.ids.messaging.requests.NotificationTemplateProvider;
import de.fraunhofer.ids.messaging.requests.RequestTemplateProvider;
import de.fraunhofer.ids.messaging.requests.enums.Crud;
import de.fraunhofer.ids.messaging.requests.enums.ProtocolType;
import de.fraunhofer.ids.messaging.requests.exceptions.RejectionException;
import de.fraunhofer.ids.messaging.requests.exceptions.UnexpectedPayloadException;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * RequestBuilder for messages with subject 'access token'.
 *
 * @param <T> Type of expected Payload.
 */
public class AccessTokenRequestBuilder<T> extends IdsRequestBuilder<T> implements ExecutableBuilder<T>, SupportsMultipart<T, AccessTokenRequestBuilder<T>> {

    AccessTokenRequestBuilder(
            final Class<T> expected,
            final MessageService messageService,
            final RequestTemplateProvider requestTemplateProvider,
            final NotificationTemplateProvider notificationTemplateProvider) {
        super(expected, messageService, requestTemplateProvider, notificationTemplateProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokenRequestBuilder<T> withPayload(final Object payload) {
        this.optPayload = Optional.ofNullable(payload);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokenRequestBuilder<T> throwOnRejection() {
        this.throwOnRejection = true;
        return this;
    }

    /**
     * Set the operation to RECEIVE: describes an {@link de.fraunhofer.iais.eis.AccessTokenRequestMessage}.
     *
     * @return this builder instance
     */
    public AccessTokenRequestBuilder<T> operationGet() {
        this.operation = Crud.RECEIVE;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageContainer<T> execute(final URI target)
            throws DapsTokenManagerException,
            ShaclValidatorException,
            SerializeException,
            ClaimsException,
            UnknownResponseException,
            SendMessageException,
            MultipartParseException,
            IOException,
            DeserializeException,
            RejectionException,
            UnexpectedPayloadException {
        if (protocolType == null || operation == null) {
            var errorMessage = String.format(
                    "Could not send Message, needed Fields are null: %s%s",
                    protocolType == null ? "protocolType is null! " : "",
                    operation == null ? "operation is null! " : ""
            );
            throw new SendMessageException(errorMessage);
        }
        //send ArtifactRequestMessage with settings:
        switch (protocolType) {
            case IDSCP:
                throw new UnsupportedOperationException("Not yet implemented Protocol!");
            case LDP:
                throw new UnsupportedOperationException("Not yet implemented Protocol!");
            case MULTIPART:
                switch (operation) {
                    case RECEIVE:
                        //build and send artifact request message
                        var message = requestTemplateProvider.accessTokenRequestMessageTemplate()
                                .buildMessage();
                        return sendMultipart(target, message);
                    default:
                        throw new UnsupportedOperationException("Unsupported Operation!");
                }
            default:
                throw new UnsupportedOperationException("Unsupported Protocol!");
        }
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public AccessTokenRequestBuilder<T> useIDSCP() {
//        this.protocolType = ProtocolType.IDSCP;
//        return this;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public AccessTokenRequestBuilder<T> useLDP() {
//        this.protocolType = ProtocolType.LDP;
//        return this;
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokenRequestBuilder<T> useMultipart() {
        this.protocolType = ProtocolType.MULTIPART;
        return this;
    }
}
