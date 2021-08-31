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
package de.fraunhofer.ids.messaging.core.config;

/**
 * Interceptor, allowing to perform an action on the configContainer,
 * right after it was created in the {@link ConfigProducer}.
 */
public interface PostConfigProducerInterceptor {

    /**
     * Perform a custom action on the generated configuration.
     *
     * @param configContainer Container managing configuration, connector and clientprovider.
     * @throws ConfigProducerInterceptorException If an internal error occurs.
     */
    void perform(ConfigContainer configContainer) throws ConfigProducerInterceptorException;

}
