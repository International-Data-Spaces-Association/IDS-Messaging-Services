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
package ids.messaging.requests.builder;

/**
 * Combining interface for protocol support. Used when builder supports all 3 protocols.
 *
 * @param <T> Expected Return type of Request Builder.
 * @param <S> The RequestBuilder returned by the internal method.
 */
public interface SupportsAllProtocols<T, S extends IdsRequestBuilder<T> & ExecutableBuilder<T>>
        extends SupportsMultipart<T, S>, SupportsLDP<T, S>, SupportsIDSCP<T, S> {
}
