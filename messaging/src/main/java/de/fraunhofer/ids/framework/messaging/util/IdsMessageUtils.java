package de.fraunhofer.ids.framework.messaging.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.security.*;
import java.util.*;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methods to hash and sign. Necessary for IDS-Messages.
 */
public class IdsMessageUtils {
    private static final Logger         LOGGER    = LoggerFactory.getLogger(IdsMessageUtils.class);
    private static final Base64.Encoder encoder64 = Base64.getEncoder();
    private static final Serializer     ser       = new Serializer();

    /**
     * Hash a value with a given MessageDigest
     *
     * @param digest MessageDigest to hash with
     * @param value  String to hash.
     *
     * @return Hash value of the input String
     */
    public static String hash( MessageDigest digest, String value ) {
        digest.update(value.getBytes());
        return encoder64.encodeToString(digest.digest());
    }

    /**
     * Generate a signature over a given String value
     *
     * @param privateSignature Signature method
     * @param value            String to sign
     * @param privateKey       Private Key to sign with.
     *
     * @return Signature as String
     *
     * @throws InvalidKeyException if the private key is invalid.
     * @throws SignatureException  if the signature cannot properly be initialized.
     */
    public static String sign( Signature privateSignature, String value, PrivateKey privateKey )
            throws InvalidKeyException, SignatureException {
        privateSignature.initSign(privateKey);
        privateSignature.update(value.getBytes());
        return encoder64.encodeToString(privateSignature.sign());
    }

    /**
     * Takes generic elements and returns them as a ArrayList
     *
     * @param <T>      type of objects in the returned ArrayList
     * @param elements elements to be put in the list
     *
     * @return elements as {@code ArrayList<T>}
     */
    @SafeVarargs
    public static <T> ArrayList<T> asList( T... elements ) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * Helper Function for accessing Info from pom.xml
     * This will read from the generated file target/classes/.../project.properties
     *
     * @param property like version, artifactID etc
     *
     * @return the pom value
     */
    public static String getProjectProperty( String property ) {

        //read /main/resources/project/properties
        LOGGER.debug(String.format("Trying to read Property %s from pom.xml properties", property));
        Properties properties = new Properties();
        try {
            properties.load(
                    Objects.requireNonNull(IdsMessageUtils.class.getClassLoader().getResourceAsStream("project.properties")));
        } catch( IOException e ) {
            LOGGER.info(e.getMessage());
        }

        //get property (might be null if not correct)
        return properties.getProperty(property);
    }

    /**
     * Generates a XML gregorian calendar from the current time.
     *
     * @return XMLGregorianCalendar containing the current time stamp as {@link XMLGregorianCalendar}.
     */
    public static XMLGregorianCalendar getGregorianNow() {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch( DatatypeConfigurationException e ) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    /**
     * Get a ConfigurationModel as JsonLD
     *
     * @param model a ConfigurationModel
     *
     * @return the SelfDeclaration of the configured connector
     *
     * @throws IOException when the connector cannot be serialized
     */
    public static String buildSelfDeclaration( ConfigurationModel model ) throws IOException {
        return ser.serialize(model.getConnectorDescription());
    }

    /**
     * Get a Connector as JsonLD
     *
     * @param model a ConfigurationModel
     *
     * @return the SelfDeclaration of the configured connector
     *
     * @throws IOException when the connector cannot be serialized
     */
    public static String buildSelfDeclaration( Connector model ) throws IOException {
        return ser.serialize(model);
    }
}
