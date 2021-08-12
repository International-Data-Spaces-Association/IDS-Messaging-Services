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
 * This class provides the the connector fingerprint, which is important for the
 * communication with the DAPS and is generated from the AKI and SKI of the connector certificate.
 */
public final class ConnectorFingerprintHandler {
    /**
     * The connector fingerprint generated during the initialization of the KeyStoreManager,
     * which is required for the connection to the DAPS. Generated using information from
     * the connector certificate, for which AKI and SKI in the connector certificate are required
     * for successful generation. If connector certificate is invalid, the connector fingerprint
     * can not be generated and will be the default INVALID_FINGERPRINT.
     */
    public static String connectorFingerprint = "INVALID_FINGERPRINT";

    private ConnectorFingerprintHandler() {
        //Nothing to do here.
    }
}
