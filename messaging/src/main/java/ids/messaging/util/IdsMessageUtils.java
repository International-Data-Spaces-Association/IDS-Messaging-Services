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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Properties;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import lombok.extern.slf4j.Slf4j;

/**
 * Methods to hash and sign. Necessary for IDS-Messages.
 */
@Slf4j
public final class IdsMessageUtils {

    /**
     * A Base64 encoder.
     */
    private static final Base64.Encoder ENCODER_64 = Base64.getEncoder();

    /**
     * The infomodel serializer.
     */
    private static final Serializer SERIALIZER = new Serializer();

    private IdsMessageUtils() {
        //Nothing to do here.
    }

    /**
     * Hash a value with a given MessageDigest.
     *
     * @param digest MessageDigest to hash with.
     * @param value String to hash.
     *
     * @return Hash value of the input String.
     */
    public static String hash(final MessageDigest digest, final String value) {
        digest.update(value.getBytes());
        return ENCODER_64.encodeToString(digest.digest());
    }

    /**
     * Generate a signature over a given String value.
     *
     * @param privateSignature Signature method.
     * @param value String to sign.
     * @param privateKey Private Key to sign with.
     * @return Signature as String.
     * @throws InvalidKeyException If the private key is invalid.
     * @throws SignatureException If the signature cannot properly be initialized.
     */
    public static String sign(final Signature privateSignature,
                       final String value,
                       final PrivateKey privateKey)
            throws InvalidKeyException, SignatureException {

        privateSignature.initSign(privateKey);
        privateSignature.update(value.getBytes());

        return ENCODER_64.encodeToString(privateSignature.sign());
    }

    /**
     * Takes generic elements and returns them as a ArrayList.
     *
     * @param <T> Type of objects in the returned ArrayList.
     * @param elements Elements to be put in the list.
     * @return Elements as {@code ArrayList<T>}.
     */
    @SafeVarargs
    public static <T> ArrayList<T> asList(final T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * Helper Function for accessing Info from pom.xml.
     * This will read from the generated file target/classes/.../project.properties
     *
     * @param property Like version, artifactID etc.
     * @return The pom value.
     * @throws IOException If the pom and its data can not be read.
     */
    public static String getProjectProperty(final String property) throws IOException {

        //read /main/resources/project/properties
        if (log.isDebugEnabled()) {
            log.debug("Trying to read Property from pom.xml properties. [code=(IMSMED0142),"
                      + " property=({})]", property);
        }

        final var properties = new Properties();

        try {
            properties.load(Objects.requireNonNull(
                            IdsMessageUtils.class
                                .getClassLoader()
                                .getResourceAsStream("project.properties")));
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Could not read property from pom! [code=(IMSMEE0025),"
                          + " exception=({})]", e.getMessage());
            }
            throw e;
        }

        //get property (might be null if not correct)
        return properties.getProperty(property);
    }

    /**
     * Generates a XML gregorian calendar from the current time.
     *
     * @return XMLGregorianCalendar containing the current time stamp
     * as {@link XMLGregorianCalendar}.
     */
    public static XMLGregorianCalendar getGregorianNow() {
        final var calendar = new GregorianCalendar();
        calendar.setTime(new Date());

        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (DatatypeConfigurationException e) {
            if (log.isErrorEnabled()) {
                log.error("[code=(IMSMEE0026)] " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Get a ConfigurationModel as JsonLD.
     *
     * @param model A ConfigurationModel.
     * @return The SelfDeclaration of the configured connector.
     * @throws IOException When the connector cannot be serialized.
     */
    public static String buildSelfDeclaration(final ConfigurationModel model) throws IOException {
        return SERIALIZER.serialize(model.getConnectorDescription());
    }

    /**
     * Get a Connector as JsonLD.
     *
     * @param model A ConfigurationModel.
     * @return The SelfDeclaration of the configured connector.
     * @throws IOException When the connector cannot be serialized.
     */
    public static String buildSelfDeclaration(final Connector model) throws IOException {
        return SERIALIZER.serialize(model);
    }
}
