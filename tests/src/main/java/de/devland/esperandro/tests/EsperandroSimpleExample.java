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
import de.devland.esperandro.annotations.GenerateStringResources;
import de.devland.esperandro.annotations.SharedPreferences;

@GenerateStringResources
@SharedPreferences(name = "simple", mode = SharedPreferenceMode.PRIVATE)
public interface EsperandroSimpleExample extends SharedPreferenceActions {

    @Default(ofInt = 42)
    int getIntegerPref();

    void setIntegerPref(int pref);

    @Default(ofLong = 42L)
    long getLongPref();

    void setLongPref(long longPref);

    @Default(ofFloat = 4.2f)
    float getFloatPref();

    void setFloatPref(float pref);

    @Default(ofBoolean = true)
    boolean getBoolPref();

    void setBoolPref(boolean pref);

    @Default(ofString = "The truth is out there...")
    String getStringPref();

    void setStringPref(String pref);

    Set<String> getStringSetPref();

    void setStringSetPref(Set<String> pref);

    @Default(ofString = "check the return value...")
    String getStringPrefWithBooleanPutter();

    boolean setStringPrefWithBooleanPutter(String value);

}
