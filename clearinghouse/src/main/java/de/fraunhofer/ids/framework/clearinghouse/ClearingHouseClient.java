package de.fraunhofer.ids.framework.clearinghouse;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryLanguage;
import de.fraunhofer.iais.eis.QueryScope;
import de.fraunhofer.iais.eis.QueryTarget;
import okhttp3.Response;

public interface ClearingHouseClient {

    /**
     * Send a LogMessage with some random pid to ClearingHouse
     *
     * @param messageToLog Infomodel Message that should be Logged
     * @return Response from ClearingHouse
     * @throws ClearingHouseClientException when some error occurs while sending the message to the ClearingHouse
     */
    Response sendLogToClearingHouse(Message messageToLog) throws ClearingHouseClientException;

    /**
     * Send a LogMessage with given pid to ClearingHouse
     *
     * @param messageToLog Infomodel Message that should be Logged
     * @param pid process id under which the message will be logged
     * @return Response from ClearingHouse
     * @throws ClearingHouseClientException when some error occurs while sending the message to the ClearingHouse
     */
    Response sendLogToClearingHouse(Message messageToLog, String pid) throws ClearingHouseClientException;

    /**
     * Query the Clearing House (Currently not working correctly @ ClearingHouse, HTTP 500)
     *
     * @param pid process id to Query (or null when querying whole clearingHouse)
     * @param messageid message id to Query (or null when querying whole process if pid is given)
     * @param queryLanguage Language of the Query
     * @param queryScope Scope of the Query
     * @param queryTarget Target of the Query
     * @param query QueryString
     * @return Response from ClearingHouse
     * @throws ClearingHouseClientException when some error occurs while sending the message to the ClearingHouse
     */
    Response queryClearingHouse(String pid, String messageid, QueryLanguage queryLanguage, QueryScope queryScope, QueryTarget queryTarget, String query) throws ClearingHouseClientException;

}
