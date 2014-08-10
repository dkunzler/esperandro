package de.devland.esperandro;

import de.devland.esperandro.serialization.JacksonSerializer;

public class SerializationJacksonTest extends SerializationBaseTest {
    @Override
    protected void setSerializer() {
        Esperandro.setSerializer(new JacksonSerializer());
    }
}
