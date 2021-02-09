package de.fraunhofer.ids.framework.daps;

/**
 * Exception which is thrown when the Response from the DAPS is empty
 */
public class DapsEmptyResponseException extends DapsTokenManagerException {

    /**
     * For Throwing a EmptyDapsResponseException with a custom error message
     *
     * @param message the error message to be included with the exception
     */
    public DapsEmptyResponseException( String message) {
        super(message);
    }

}
