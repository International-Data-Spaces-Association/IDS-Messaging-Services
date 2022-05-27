/*
 * Copyright Fraunhofer Institute for Software and Systems Engineering
 *
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
 *
 *  Contributors:
 *       sovity GmbH
 *
 */
package ids.messaging.core.config.util;

import java.util.Optional;

/**
 * This class provides static access to the aki/ski fingerprint of the connector,
 * by its certificate.
 */
public final class ConnectorFingerprintProvider {
    /**
     * AKI/SKI connector fingerprint. Value is initialized by the KeyStoreManager and reset at
     * each update (e.g. connector certificate change). Will be empty if no fingerprint could be
     * determined (e.g. invalid or no connector certificate).
     */
    public static Optional<String> fingerprint;

    private ConnectorFingerprintProvider() {
        //Nothing to do here.
    }
}
