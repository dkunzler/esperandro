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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

public class GsonSerializer implements Serializer {

    private Gson gson = new GsonBuilder().create();


    @Override
    public String serialize(Serializable object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T deserialize(String serializedObject, Class<T> clazz) {
        T deserialized = null;
        if (serializedObject != null) {
            deserialized = gson.fromJson(serializedObject, clazz);
        }
        return deserialized;
    }
}
