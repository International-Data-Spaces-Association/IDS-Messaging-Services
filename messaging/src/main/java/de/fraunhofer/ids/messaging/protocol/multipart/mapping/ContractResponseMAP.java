/*
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
 */
package de.fraunhofer.ids.messaging.protocol.multipart.mapping;

import java.util.Optional;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractResponseMessage;
import de.fraunhofer.ids.messaging.protocol.multipart.SerializedPayload;

public class ContractResponseMAP
        extends AbstractContractMAP<ContractResponseMessage, ContractOffer> {

    public ContractResponseMAP(final ContractResponseMessage contractResponseMessage,
                               final ContractOffer payload) {
        this.message = contractResponseMessage;
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContractResponseMessage getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Contract> getPayload() {
        return Optional.of(payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.toRdf().getBytes(),
                                     "application/ld+json",
                                     payload.getId().toString());
    }
}
