package de.fraunhofer.ids.framework.util;

public enum MultipartDatapart {
    HEADER("header"),
    PAYLOAD("payload");

    private final String name;

    MultipartDatapart( String name ) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
