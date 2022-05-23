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
package ids.messaging.requests.exceptions;

import de.fraunhofer.iais.eis.RejectionReason;
import lombok.Getter;

/**
 * Exception which could be thrown upon receiving a rejection message.
 */
@Getter
public class RejectionException extends IdsRequestException {

    /**
     * The RejectionReason.
     */
    private RejectionReason rejectionReason;

    /**
     * Constructor for the RejectionException.
     *
     * @param rejectionReason The rejection reason.
     */
    public RejectionException(final RejectionReason rejectionReason) {
        super();
        this.rejectionReason = rejectionReason;
    }

    /**
     * Constructor for the RejectionException.
     *
     * @param message The exception message.
     * @param rejectionReason The rejection reason.
     */
    public RejectionException(final String message, final RejectionReason rejectionReason) {
        super(message);
        this.rejectionReason = rejectionReason;
    }
}
