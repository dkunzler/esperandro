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
import de.devland.esperandro.annotations.experimental.GenerateStringResources;

@GenerateStringResources
@SharedPreferences(name = "simple", mode = SharedPreferenceMode.PRIVATE)
public interface EsperandroSimpleExample extends SharedPreferenceActions {

    @Default(ofInt = 42)
    int integerPref();

    void integerPref(int pref);

    @Default(ofLong = 42L)
    long longPref();

    void longPref(long longPref);

    @Default(ofFloat = 4.2f)
    float floatPref();

    void floatPref(float pref);

    @Default(ofBoolean = true)
    boolean boolPref();

    void boolPref(boolean pref);

    @Default(ofString = "The truth is out there...")
    String stringPref();

    void stringPref(String pref);

    Set<String> stringSetPref();

    void stringSetPref(Set<String> pref);

    @Default(ofString = "check the return value...")
    String stringPrefWithBooleanPutter();

    boolean stringPrefWithBooleanPutter(String value);

}
