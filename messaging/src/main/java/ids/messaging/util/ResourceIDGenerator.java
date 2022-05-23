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
package ids.messaging.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Utility for generating Resource IDs for infomodel builders.
 */
public final class ResourceIDGenerator {

    /**
     * Base autogen URL to use.
     */
    private static final String URI_BASE = "https://w3id.org/idsa/autogen";

    private ResourceIDGenerator() { }

    /**
     * Create an URI with callerClazz name and random uuid in path (used as ID URIs).
     *
     * @param callerClazz Class for which the randomURI should be generated.
     * @return A random URI ID.
     */
    public static URI randomURI(final Class<?> callerClazz) {
        try {
            return new URI(String.format("%s/%s/%s", URI_BASE,
                                         callerClazz.getSimpleName(),
                                         UUID.randomUUID()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
