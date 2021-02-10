package de.fraunhofer.ids.framework.messaging.clearinghouse;

public class ClearingHouseClientException extends Exception{

    public ClearingHouseClientException(String msg){
        super(msg);
    }

    public ClearingHouseClientException(String msg, Exception cause){
        super(msg, cause);
    }

}
