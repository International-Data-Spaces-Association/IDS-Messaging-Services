package de.fraunhofer.ids.messaging.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerializerProviderTest {

    @Test
    void testProvideSerializer(){
        assertNotNull(new SerializerProvider().getSerializer());
    }

}