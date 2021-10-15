package de.fraunhofer.ids.messaging.core.config;

import java.util.List;

/**
 * Provider for currently used Infomodel and compatible inbound versions.
 */
public class InfomodelVersionProvider {

    /**
     * Currently used infomodel version.
     */
    public static final String OUTBOUND_VERSION = "4.2.4";

    /**
     * Supported inbound infomodel versions.
     */
    public static final List<String> INBOUND_VERSIONS = List.of(
            "4.0.0", "4.1.0", "4.1.2", "4.2.0", "4.2.1", "4.2.2", "4.2.3", "4.2.4"
    );
}
