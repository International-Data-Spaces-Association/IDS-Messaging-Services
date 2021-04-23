package de.fraunhofer.ids.framework.util;

public enum MultipartDatapart {
    HEADER("header"),
    PAYLOAD("payload");

    private final String name;

    MultipartDatapart(final String name) {
        this.name = name;
    }

    /**
     * One way to get the Name of the enum-item.
     *
     * @return name of tje enum item
     */
    public String toString() {
        return this.name;
    }
}
