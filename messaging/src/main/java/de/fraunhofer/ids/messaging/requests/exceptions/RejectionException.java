package de.fraunhofer.ids.messaging.requests.exceptions;

import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionReason;

public class RejectionException extends IdsRequestException{

    private RejectionReason rejectionReason;

    public RejectionException(RejectionReason rejectionReason){super(); this.rejectionReason = rejectionReason;}

    public RejectionException(String message, RejectionReason rejectionReason){super(message); this.rejectionReason = rejectionReason;}
}
