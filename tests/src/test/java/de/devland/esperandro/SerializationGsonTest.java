package de.devland.esperandro;

import de.devland.esperandro.serialization.GsonSerializer;

public class SerializationGsonTest extends SerializationBaseTest {
    @Override
    protected void setSerializer() {
        Esperandro.setSerializer(new GsonSerializer());
    }
}
