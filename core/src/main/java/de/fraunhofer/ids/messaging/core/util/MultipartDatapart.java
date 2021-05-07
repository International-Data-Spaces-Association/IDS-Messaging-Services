package de.fraunhofer.ids.messaging.core.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum MultipartDatapart {
    HEADER("header"),
    PAYLOAD("payload");

    String name;

    /**
     * One way to get the Name of the enum-item.
     *
     * @return name of tje enum item
     */
    @Override
    public String toString() {
        return this.name;
    }
}
