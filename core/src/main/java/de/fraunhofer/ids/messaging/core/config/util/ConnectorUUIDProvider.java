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

import java.util.UUID;

/**
 * This class provides the the connector UUID, which is imported from the
 * connector certificate.
 */
public final class ConnectorUUIDProvider {
    /**
     * The connector UUID set during the initialization of the KeyStoreManager.
     * If connector certificate is invalid, the connector UUID
     * will be initialized as a random UUID.
     */
    public static UUID connectorUUID = UUID.randomUUID();

    private ConnectorUUIDProvider() {
        //Nothing to do here.
    }
}
