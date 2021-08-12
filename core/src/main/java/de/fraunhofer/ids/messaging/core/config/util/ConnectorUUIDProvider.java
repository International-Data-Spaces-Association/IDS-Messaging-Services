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
package de.fraunhofer.ids.messaging.core.config.util;

/**
 * This class provides the retrieval of the connector UUID, which is important for the
 * communication with the DAPS and is generated from the AKI and SKI of the connector certificate.
 */
public final class ConnectorUUIDProvider {
    /**
     * The Connector UUID generated during the initialization of the KeyStoreManager,
     * which is required for the connection to the DAPS. Generated using information from
     * the connector certificate, for which AKI and SKI in the connector certificate are required
     * for successful generation. If an invalid certificate is present, it remains with a default
     * connector UUID, where all numbers are 0.
     */
    public static String connectorUUID = "00:00:00:00:00:00:00:00:00:00:00:00:"
         + "00:00:00:00:00:00:00:00:keyid:00:00:00:00:00:00:00:00:00:00:00:00:"
         + "00:00:00:00:00:00:00:00";

    /**
     * This boolean value allows to query if during the initialization of the KeyStoreManager
     * the connector UUID could be generated successfully or if it is probably the default
     * connector UUID. True if connector UUID could be generated.
     */
    public static boolean validUUID = false;

    private ConnectorUUIDProvider() {
        //Nothing to do here.
    }
}
