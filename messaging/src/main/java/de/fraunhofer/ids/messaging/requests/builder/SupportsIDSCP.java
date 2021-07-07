package de.fraunhofer.ids.messaging.requests.builder;

/**
 * Used for Builders which support IDSCP Protocol
 *
 * @param <T> Expected Return type of Request Builder
 * @param <S> The RequestBuilder returned by the internal method
 */
@FunctionalInterface
public interface SupportsIDSCP<T, S extends IdsRequestBuilder<T> & ExecutableBuilder<T>> {

    /**
     * @return same builder instance (or specific subtype when supported operations are different) with protocol set to IDSCP.
     */
    S useIDSCP();

}
