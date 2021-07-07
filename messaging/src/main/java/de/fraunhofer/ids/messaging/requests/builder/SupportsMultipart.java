package de.fraunhofer.ids.messaging.requests.builder;

import de.fraunhofer.ids.messaging.requests.enums.ProtocolType;

public interface SupportsMultipart<T, S extends IdsRequestBuilder<T> & ExecutableBuilder<T>> {

    S useMultipart();

}
