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
package ids.messaging.dispatcher.filter;

import lombok.NoArgsConstructor;

/**
 * Builder class for PreDispatchingFilterResults.
 */
@NoArgsConstructor
public class PreDispatchingFilterResultBuilder {

    /**
     * Possible error to throw if not successful.
     */
    private Throwable error;

    /**
     * True if filter was successful.
     */
    private boolean success;

    /**
     * The message.
     */
    private String message;

    /**
     * Error message of the PreDispatchingFilter.
     *
     * @param error The throwable error.
     * @return PreDispatchingFilterResultBuilder.
     */
    public PreDispatchingFilterResultBuilder withError(final Throwable error) {
        this.error = error;
        return this;
    }

    /**
     * Success indicator of the PreDispatchingFilter.
     *
     * @param success Boolean if filter was successful.
     * @return PreDispatchingFilterResultBuilder.
     */
    public PreDispatchingFilterResultBuilder withSuccess(final boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Message of the PreDispatchingFilter.
     *
     * @param message Message of the PreDispatchingFilter.
     * @return PreDispatchingFilterResultBuilder.
     */
    public PreDispatchingFilterResultBuilder withMessage(final String message) {
        this.message = message;
        return this;
    }

    /**
     * Build the PreDispatchingFilter-Result.
     *
     * @return The build PreDispatchingFilter-Res.ult
     */
    public PreDispatchingFilterResult build() {
        return new PreDispatchingFilterResult(error, success, message);
    }
}
