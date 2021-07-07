package de.fraunhofer.ids.messaging.requests.builder;

/**
 * Combining interface for protocol support. Used when builder supports all 3 protocols.
 *
 * @param <T> Expected Return type of Request Builder
 * @param <S> The RequestBuilder returned by the internal method
 */
public interface SupportsAllProtocols<T, S extends IdsRequestBuilder<T> & ExecutableBuilder<T>>
        extends SupportsMultipart<T, S>, SupportsLDP<T, S>, SupportsIDSCP<T, S> {
}
