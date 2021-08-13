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
package de.fraunhofer.ids.messaging.core.config.util;

import java.util.Properties;

/**
 * This class returns the current versions of the infomodel artifacts.
 */
public class InfomodelArtifactsVersionProvider {
    /**
     * Determines the version of the infomodel java artifact in use.
     * @return Version number as string, e.g. "4.1.0"
     * @throws VersionDeterminationException Thrown if either the dependency is not present
     * or the artifact properties do not show a "version".
     */
    public String getArtifactJavaVersion() throws VersionDeterminationException {
        return getDependencyVersion(
                "/META-INF/maven/de.fraunhofer.iais.eis.ids.infomodel/java/pom.properties");
    }

    /**
     * Determines the version of the infomodel serializer artifact in use.
     * @return Version number as string, e.g. "4.1.0"
     * @throws VersionDeterminationException Thrown if either the dependency is not present
     * or the artifact properties do not show a "version".
     */
    public String getArtifactSerializerVersion() throws VersionDeterminationException {
        return getDependencyVersion(
                "/META-INF/maven/de.fraunhofer.iais.eis.ids/infomodel-serializer/pom.properties");
    }

    /**
     * Determines the version of the infomodel interaction artifact in use (SHACL-Validation).
     * @return Version number as string, e.g. "4.1.0"
     * @throws VersionDeterminationException Thrown if either the dependency is not present
     * or the artifact properties do not show a "version".
     */
    public String getArtifactInteractionVersion() throws VersionDeterminationException {
        return getDependencyVersion(
                "/META-INF/maven/de.fraunhofer.iais.eis.ids/interaction/pom.properties");
    }

    private String getDependencyVersion(final String artifact)
            throws VersionDeterminationException {
        try {
            final var properties = new Properties();
            final var is = getClass().getResourceAsStream(artifact);
            properties.load(is);
            return properties.getProperty("version", "");
        } catch (Exception exception) {
            throw new VersionDeterminationException("Could not determine artifact version!",
                                                    exception.getCause());
        }
    }
}
