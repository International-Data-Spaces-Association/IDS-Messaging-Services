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

import java.security.Key;

/**
 * An Implementation of this has to provide the Public Key from the DAPS Service.
 */
public interface DapsPublicKeyProvider {
    /**
     * Try to get the Public Key using the kid from jwks of issuer DAPS.
     *
     * @param issuer Issuer of the DAT.
     * @param kid KID of public key from jwks.
     * @return publicKey of issuer DAPS (or null if it does not exist).
     */
    Key requestPublicKey(String issuer, String kid);
}
