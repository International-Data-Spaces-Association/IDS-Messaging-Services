package de.fraunhofer.ids.messaging.requests.builder;

public interface SupportsIDSCP<T, S extends IdsRequestBuilder<T> & ExecutableBuilder<T>> {

    S useIDSCP();

}
