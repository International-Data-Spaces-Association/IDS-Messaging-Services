package de.fraunhofer.ids.framework.clearinghouse;

import okhttp3.Response;

public interface ClearingHouseClient {

    Response sendLogToClearingHouse(String logMessage) throws ClearingHouseClientException;

}
