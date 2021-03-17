package de.fraunhofer.ids.framework.broker;

import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.ids.framework.daps.ClaimsException;
import de.fraunhofer.ids.framework.daps.DapsTokenManagerException;
import okhttp3.Response;
import org.apache.commons.fileupload.FileUploadException;

import java.io.IOException;
import java.util.List;

/**
 * Interface for Communication with IDS Brokers, implemented by {@link BrokerService}
 */
public interface IDSBrokerService {

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ResourceUnavailableMessage} to the broker.
     * The given Resource will be unregistered from the broker.
     *
     * @param brokerURI URI of the broker the connector will try to unregister the resource at
     * @param resource the resource that will be unregistered at the broker
     * @return the ResponseMessage of the Broker
     * @throws IOException if the built message could not be serialized
     */
    Response removeResourceFromBroker(String brokerURI, Resource resource) throws IOException, DapsTokenManagerException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage} to the broker.
     * The Connector will be registered at the broker, or its selfdescription will be updated.
     *
     * @param brokerURI URI of the broker the connector will try to unregister the resource at
     * @param resource the resource that will be unregistered at the broker
     * @return the ResponseMessage of the Broker
     * @throws IOException if the built message could not be serialized
     */
    Response updateResourceAtBroker(String brokerURI, Resource resource) throws IOException, DapsTokenManagerException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUnavailableMessage} to the broker.
     * The Connector will be unregistered from the broker.
     *
     * @param brokerURI URI of the broker the connector will try to unregister at
     *
     * @return the ResponseMessage of the Broker (NotificationMessage if it worked, RejectionMessage if not)
     *
     * @throws IOException if the message could not be serialized
     */
    Response unregisterAtBroker( String brokerURI )
            throws IOException, DapsTokenManagerException, ClaimsException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage} to the broker.
     * The currently available connector self declaration at the broker will be updated. In order to update the
     * connector uuid in the self declaration has to be the same as the registered one at the broker.
     *
     * @param brokerURI URI of the broker the connector will try to update its information at
     * @return the ResponseMessage of the Broker (NotificationMessage if it worked, RejectionMessage if not)
     * @throws IOException if the built message could not be serialized
     */
    Response updateSelfDescriptionAtBroker(String brokerURI) throws IOException, DapsTokenManagerException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.ConnectorUpdateMessage} to a list of brokers
     *
     * @param brokerURIs URIs of the brokers the connector will try to update its information at
     *
     * @return a List of Responses from the Broker
     *
     * @throws IOException if the built message could not be serialized
     */
    List<Response> updateSelfDescriptionAtBrokers( List<String> brokerURIs )
            throws IOException, DapsTokenManagerException;

    /**
     * Builds and sends a {@link de.fraunhofer.iais.eis.QueryMessage} to the broker.
     *
     * @param brokerURI     the URI of the broker the message is sent to
     * @param query         the query as payload for the QueryMessage
     * @param queryLanguage the Language of the Query (e.g. SPARQL, SQL, XQUERY). See {@link QueryLanguage}
     * @param queryScope the Scope of the Query (ALL connectors, ACTIVE connectors, INACTIVE connectors). See {@link QueryScope}
     * @param queryTarget the type of IDS Components that are queried. See {@link QueryTarget}
     * @return the brokers response to the query request
     * @throws IOException if the built message could not be serialized
     */
    Response queryBroker(String brokerURI, String query, QueryLanguage queryLanguage, QueryScope queryScope, QueryTarget queryTarget)
            throws IOException, DapsTokenManagerException;
}
