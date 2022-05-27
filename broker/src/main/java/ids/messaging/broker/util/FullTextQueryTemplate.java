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
package ids.messaging.broker.util;

/**
 * Class links to the fulltext query template within the messaging module.
 */
public final class FullTextQueryTemplate {
    /**
     * Refers to the template for the fulltext query search.
     */
    public static final String FULL_TEXT_QUERY =
            ids.messaging.util.FullTextQueryTemplate.FULL_TEXT_QUERY;

    private FullTextQueryTemplate() {
        // Nothing to do here.
    }
}
