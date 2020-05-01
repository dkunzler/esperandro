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

package de.devland.esperandro;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.Set;

import de.devland.esperandro.tests.EsperandroDefaultsExample;
import de.devland.esperandro.tests.model.Container;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class DefaultsTest {

    private EsperandroDefaultsExample preferences;

    @Before
    public void setup() {
        preferences = Esperandro.getPreferences(EsperandroDefaultsExample.class,
                RuntimeEnvironment.application);
    }

    @After
    public void tearDown() {
        preferences.clearAll();
    }

    @Test
    public void annotationDefaults() {
        Assert.assertNotNull(preferences);

        Assert.assertTrue(preferences.getBoolPref());
        // check if we get the default value, if the pref isn't set yet
        Assert.assertTrue(preferences.getBoolPref(true));
        // set the pref to false
        preferences.setBoolPref(false);
        // verify we get the pref value, not the runtime default
        Assert.assertFalse(preferences.getBoolPref(true));

        Assert.assertFalse(preferences.getBoolPref(false));

        Assert.assertEquals(42, preferences.getIntegerPref());

        Assert.assertEquals(42l, preferences.getLongPref());

        Assert.assertEquals(4.2f, preferences.getFloatPref());

        Assert.assertNull(preferences.getStringSetPref());
        Set<String> exampleSet = new HashSet<String>();
        exampleSet.add("test");
        Set<String> preferenceSet = preferences.getStringSetPref(exampleSet);
        Assert.assertEquals(exampleSet, preferenceSet);
        Assert.assertEquals("test", preferenceSet.iterator().next());

        Assert.assertNull(preferences.getComplexPref());
        Container exampleComplex = new Container();
        exampleComplex.value = "test";
        exampleComplex.anotherValue = 23;
        Assert.assertEquals(exampleComplex, preferences.getComplexPref(exampleComplex));
    }

    @Test
    public void initDefaults() {
        Assert.assertFalse(preferences.contains("boolPref"));
        boolean defaultValue = preferences.getBoolPref();
        preferences.initDefaults();
        Assert.assertTrue(preferences.contains("boolPref"));
        Assert.assertEquals(defaultValue, preferences.getBoolPref());
        preferences.setBoolPref(!defaultValue);
        Assert.assertEquals(!defaultValue, preferences.getBoolPref());
        preferences.initDefaults();
        // initDefaults should not override user-set value
        Assert.assertEquals(!defaultValue, preferences.getBoolPref());
    }


}
