package de.devland.esperandro;

import android.content.Context;
import android.util.Log;
import de.devland.esperandro.serialization.Serializer;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

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
 *
 */

/**
 * Manager to give access to the generated Esperandro-SharedPreference implementations.
 */
public class Esperandro {
    private static Esperandro instance;

    private static final String SUFFIX = "$$Impl";

    private static final String TAG = "Esperandro";

    private Esperandro() {}

    private static Esperandro getInstance() {
        if (instance == null) {
            synchronized (Esperandro.class) {
                if (instance == null) {
                    instance = new Esperandro();
                }
            }
        }
        return instance;
    }

    private Serializer serializer;

    private final Map<String, Object> preferenceInstances = new HashMap<String, Object>();

    /**
     * Returns an instance of the pre-generated class of the given SharedPreferences-annotated interface.
     *
     * @param preferenceClass
     *         The interface whose implementation should be returned.
     * @param context
     *         A context to be able to construct the android SharedPreference.
     *
     * @return An instance of the given interface.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPreferences(Class<T> preferenceClass, Context context) {
        Esperandro esperandro = Esperandro.getInstance();

        T implementation = (T) esperandro.preferenceInstances.get(preferenceClass.getName());
        if (implementation == null) {
            implementation = esperandro.createInstance(preferenceClass, context);
            esperandro.preferenceInstances.put(preferenceClass.getName(), implementation);
        }
        return implementation;
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> preferenceClass, Context context) {
        T implementation;
        try {
            Class<? extends T> implementationClass = (Class<? extends T>) Class.forName(preferenceClass
                    .getCanonicalName() + SUFFIX);
            Constructor<? extends T> constructor = implementationClass.getConstructor(Context.class);
            implementation = constructor.newInstance(context);

        } catch (Exception e) {
            throw new RuntimeException("Couldn't load generated class. Please check esperandro processor " +
                    "configuration in your project.", e);
        }
        return implementation;
    }

    public static void setSerializer(Serializer serializer) {
        Esperandro.getInstance().serializer = serializer;
    }

    public static Serializer getSerializer() {
        Esperandro esperandro = Esperandro.getInstance();
        if (esperandro.serializer == null) {
            esperandro.serializer = getDefaultSerializer();
            if (esperandro.serializer == null) {
                throw new IllegalStateException("Tried to save a serialized Object into preferences but no serializer" +
                        " is " +
                        "" + "present");
            }
        }
        return esperandro.serializer;
    }

    @SuppressWarnings("unchecked")
    private static Serializer getDefaultSerializer() {
        Serializer defaultSerializer = null;
        try {
            Class<? extends Serializer> defaultSerializerClass = (Class<? extends Serializer>) Class.forName("de" + "" +
                    ".devland.esperandro.serialization.GsonSerializer");
            defaultSerializer = defaultSerializerClass.newInstance();
        } catch (Exception e) {
            Log.w(TAG, "Default Serializer (GsonSerializer) not present.");
        }

        return defaultSerializer;
    }

}
