package de.fraunhofer.ids.framework.messaging.clearinghouse;

import okhttp3.Response;

public interface ClearingHouseClient {

    Response sendLogToClearingHouse(String logMessage) throws ClearingHouseClientException;

}
