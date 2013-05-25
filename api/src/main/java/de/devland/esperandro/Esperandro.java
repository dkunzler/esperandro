package de.devland.esperandro;

import android.content.Context;

import java.lang.reflect.Constructor;

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
public class Esperandro {

    private static final String SUFFIX = "$$Impl";

    @SuppressWarnings("unchecked")
    public static <T> T getPreferences(Class<T> preferenceClass, Context context) {
        T implementation = null;
        try {
            Class<? extends T> implementationClass = (Class<? extends T>) Class
                    .forName(preferenceClass.getCanonicalName()
                            + SUFFIX);
            Constructor<? extends T> constructor = implementationClass
                    .getConstructor(Context.class);
            implementation = constructor.newInstance(context);

        } catch (Exception e) {
            // TODO nice handling
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return implementation;
    }
}
