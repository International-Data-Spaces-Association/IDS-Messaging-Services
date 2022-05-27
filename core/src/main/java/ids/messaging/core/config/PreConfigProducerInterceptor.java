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
package ids.messaging.core.config;

import de.fraunhofer.iais.eis.ConfigurationModel;

/**
 * Intercepts ConfigProducer and loads own configuration
 * (e.g from a DB instead of config.json file).
 */
public interface PreConfigProducerInterceptor {

    /**
     * Load or build a ConfigurationModel and return it (will be used by ConfigProducer to
     * generate Key/Truststore and ConfigContainer).
     *
     * @param properties Configproperties of ConfigProducer.
     * @return Loaded ConfigurationModel.
     * @throws ConfigProducerInterceptorException If loading a configuration failed.
     */
    ConfigurationModel perform(ConfigProperties properties)
            throws ConfigProducerInterceptorException;
}
