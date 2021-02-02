package de.fraunhofer.ids.framework.messaging.dispatcher.filter;

import de.fraunhofer.iais.eis.Message;

/**
 * A filter that can be used for processing the message before it gets to the MessageDispatcher
 * (can be used to filter and reject some messages after custom criteria before they get to the MessageHandler)
 */
@FunctionalInterface
public interface PreDispatchingFilter {
    /**
     * Execute the PreDispatchingFilter and return the result
     *
     * @param in the RequestMessage to be filtered
     *
     * @return result of the processing
     *
     * @throws PreDispatchingFilterException if an error occurs while processing the message
     */
    PreDispatchingFilterResult process( final Message in ) throws PreDispatchingFilterException;
}
