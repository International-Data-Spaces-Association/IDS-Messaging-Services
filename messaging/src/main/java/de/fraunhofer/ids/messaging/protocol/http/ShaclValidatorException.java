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
package de.fraunhofer.ids.messaging.protocol.http;

/**
 * SHACL Validation: Received message headers does not conform to IDS-infomodel.
 */
public class ShaclValidatorException extends Exception {
    private static final long serialVersionUID = 42L;

    /**
     * SHACL Validation: Received message headers does not conform to IDS-infomodel.
     *
     * @param message The error message.
     */
    public ShaclValidatorException(final String message) {
        super(message);
    }
}
