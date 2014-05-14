/*
 * Copyright 2013 David Kunzler
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package de.devland.esperandro.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class JacksonSerializer implements Serializer {

    private ObjectMapper objectMapper;

    public JacksonSerializer() {
        objectMapper = new ObjectMapper();
    }

    public JacksonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            String message = String.format("Object \"%s\" could not be written to a JSON String.", object);
            throw new IllegalArgumentException(message, e);
        }
    }

    @Override
    public <T> T deserialize(String serializedObject, Class<T> clazz) {
        try {
            return objectMapper.readValue(serializedObject, clazz);
        } catch (IOException e) {
            String message = String.format("Saved preference \"%s\" could not be parsed from JSON.", serializedObject);
            throw new IllegalArgumentException(message, e);
        }
    }
}
