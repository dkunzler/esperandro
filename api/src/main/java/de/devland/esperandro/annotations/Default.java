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
package de.devland.esperandro.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to set a default for the primitive preferences as well as {@code String}. A {@code Set<String>} will always
 * have {@code null} as default value;
 * <p/>
 * If no annotation is given or the proper default value is not set the used defaults are: {@code boolean}: false {@code
 * int}: -1 {@code long}: -1l {@code float}: -1.0f {@code String}: "" (empty String)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {

    public static final boolean booleanDefault = false;
    public static final int intDefault = -1;
    public static final long longDefault = -1l;
    public static final float floatDefault = -1.0f;
    public static final String stringDefault = "";

    boolean ofBoolean() default booleanDefault;

    int ofInt() default intDefault;

    long ofLong() default longDefault;

    float ofFloat() default floatDefault;

    String ofString() default stringDefault;
}
