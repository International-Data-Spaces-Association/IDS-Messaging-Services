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
package ids.messaging.core.daps;

/**
 * Interface for TokenManagerService to acquire a DAPS DAT.
 */
public interface TokenManagerService {

    /**
     * Acquire the DAPS DAT.
     *
     * @param dapsURL The URL of the DAPS.
     * @return The DAT as String.
     * @throws DapsConnectionException Thrown if no connection to DAPS possible.
     * @throws DapsEmptyResponseException Thrown if DAPS returned invalid response.
     * @throws ConnectorMissingCertExtensionException Thrown if something is
     * wrong with the Connector Certificate-
     */
    String acquireToken(String dapsURL)
            throws
            DapsConnectionException,
            DapsEmptyResponseException,
            ConnectorMissingCertExtensionException;
}
