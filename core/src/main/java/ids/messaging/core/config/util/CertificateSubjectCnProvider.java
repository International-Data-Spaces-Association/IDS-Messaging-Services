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

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides static access to the subject-CN of the connector certificate,
 * which could be used as the connector UUID depending on the implementation of the connector.
 */
@Slf4j
public final class CertificateSubjectCnProvider {
    /**
     * The subject-CN of the connector certificate, which could be used as the connector UUID
     * depending on the implementation of the connector. Should be unique per certificate
     * issued by a DAPS. Value is initialized by the KeyStoreManager and reset at each update.
     * If no valid certificate with Subject-CN is available, a random UUID is
     * generated in the KeyStoreManager instead.
     */
    public static UUID certificateSubjectCn;

    private CertificateSubjectCnProvider() {
        //Nothing to do here.
    }
}
