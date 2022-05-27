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
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Component
@Slf4j
public class PreConfigInterceptor implements PreConfigProducerInterceptor{

    private final static Serializer SERIALIZER = new Serializer();

    @Override
    public ConfigurationModel perform(final ConfigProperties properties)
            throws ConfigProducerInterceptorException {
        try {
            if (log.isInfoEnabled()) {
                log.info("Intercepting loading of configuration! [code=(IMSCOI0055)]");
            }
            final var config = loadConfig(properties);
            config.setProperty("preInterceptor", true);
            return config;
        } catch (IOException e) {
            throw new ConfigProducerInterceptorException(e.getMessage());
        }
    }

    private ConfigurationModel loadConfig(final ConfigProperties properties)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Loading configuration from {} [code=(IMSCOD0114)]",
                      properties.getPath());
        }

        final var config = getConfiguration(properties);

        if (log.isInfoEnabled()) {
            log.info("Importing configuration from file. [code=(IMSCOI0056)]");
        }

        return SERIALIZER.deserialize(config, ConfigurationModel.class);
    }

    private String getConfiguration(final ConfigProperties properties) throws IOException {
        if (Paths.get(properties.getPath()).isAbsolute()) {
            return getAbsolutePathConfig(properties);
        } else {
            return getClassPathConfig(properties);
        }
    }

    private String getClassPathConfig(final ConfigProperties properties) throws IOException {
        if (log.isInfoEnabled()) {
            log.info("Loading config from classpath: {} [code=(IMSCOI0057)]", properties.getPath());
        }

        final var configurationStream = new ClassPathResource(properties.getPath()).getInputStream();
        final var config = IOUtils.toString(configurationStream);
        configurationStream.close();

        return config;
    }

    private String getAbsolutePathConfig(final ConfigProperties properties) throws IOException {
        if (log.isInfoEnabled()) {
            log.info("Loading config from absolute Path {} [code=(IMSCOI0058)]",
                     properties.getPath());
        }

        final var fis = new FileInputStream(properties.getPath());
        final var config = IOUtils.toString(fis);
        fis.close();

        return config;
    }
}
