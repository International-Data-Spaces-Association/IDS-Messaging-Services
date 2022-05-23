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
package ids.messaging.protocol.multipart.parser;

import lombok.RequiredArgsConstructor;

/**
 * Different data parts of a multipart message.
 */
@RequiredArgsConstructor
public enum MultipartDatapart {
    /**
     * Multipart header.
     */
    HEADER("header"),

    /**
     * Multipart payload.
     */
    PAYLOAD("payload");

    /**
     * The name of the enum.
     */
    private final String name;

    /**
     * One way to get the Name of the enum-item.
     *
     * @return Name of tje enum item.
     */
    @Override
    public String toString() {
        return this.name;
    }
}
