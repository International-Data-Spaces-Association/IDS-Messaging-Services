package de.fraunhofer.ids.messaging.requests.builder;

import de.fraunhofer.ids.messaging.requests.enums.ProtocolType;

/**
 * Used for Builders which support Multipart Protocol
 *
 * @param <T> Expected Return type of Request Builder
 * @param <S> The RequestBuilder returned by the internal method
 */
@FunctionalInterface
public interface SupportsMultipart<T, S extends IdsRequestBuilder<T> & ExecutableBuilder<T>> {

    /**
     * @return same builder instance (or specific subtype when supported operations are different) with protocol set to Multipart.
     */
    S useMultipart();

}
