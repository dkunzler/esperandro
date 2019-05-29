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
package de.devland.esperandro.tests;

import java.util.Set;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.SharedPreferenceMode;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;

@SharedPreferences(name = "defaults", mode = SharedPreferenceMode.PRIVATE)
public interface EsperandroDefaultsExample extends SharedPreferenceActions {

    @Default(ofInt = 42)
    int integerPref();

    @Default(ofLong = 42L)
    long longPref();

    @Default(ofFloat = 4.2f)
    float floatPref();

    @Default(ofBoolean = true)
    boolean boolPref();

    boolean boolPref$Default(boolean defaultValue);

    void boolPref(boolean value);

    @Default(ofBoolean = false)
    boolean defaultBoolPref();

    @Default(ofString = "The truth is out there...")
    String stringPref();

    Set<String> stringSetPref();

    Set<String> stringSetPref$Default(Set<String> defaultValue);

    Container complexPref();

    Container complexPref$Default(Container defaultValue);

    @Default(ofStatement = "java.lang.Math.max(5,4)")
    int statementDefault();
    void statementDefault(int statementDefault);

    @Default(ofStatement = "new Container()")
    Container complextPrefStatementDefault();
    void complexPrefStatementDefault(Container container);

}
