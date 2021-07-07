package de.fraunhofer.ids.messaging.requests.builder;

public interface SupportsLDP<T, S extends IdsRequestBuilder<T> & ExecutableBuilder<T>> {

    S useLDP();



}
