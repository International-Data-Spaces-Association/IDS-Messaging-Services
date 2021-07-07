package de.fraunhofer.ids.messaging.requests.builder;

public interface SupportsAllProtocols<T, S extends IdsRequestBuilder<T> & ExecutableBuilder<T>>
        extends SupportsMultipart<T, S>, SupportsLDP<T, S>, SupportsIDSCP<T, S> {
}
