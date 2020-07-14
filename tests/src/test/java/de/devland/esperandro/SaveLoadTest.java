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

import android.content.SharedPreferences;
import de.devland.esperandro.tests.EsperandroSimpleExample;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class SaveLoadTest {

    private EsperandroSimpleExample esperandroPreferences;
    private SharedPreferences androidPreferences;

    @Before
    public void setup() {
        esperandroPreferences = Esperandro.getPreferences(EsperandroSimpleExample.class, RuntimeEnvironment.application);
        androidPreferences = esperandroPreferences.get();
    }

    @Test
    public void saveLoad() {
        esperandroPreferences.setBoolPref(true);
        Assert.assertTrue(androidPreferences.getBoolean("boolPref", false)); // saved correctly
        Assert.assertTrue(esperandroPreferences.getBoolPref()); // loaded correctly
        esperandroPreferences.setBoolPref(false);
        Assert.assertFalse(androidPreferences.getBoolean("boolPref", true)); // saved correctly
        Assert.assertFalse(esperandroPreferences.getBoolPref()); // loaded correctly

        esperandroPreferences.setFloatPref(3.14f);
        Assert.assertEquals(0, Float.compare(3.14f, androidPreferences.getFloat("floatPref", 0f))); // saved correctly
        Assert.assertEquals(0, Float.compare(3.14f, esperandroPreferences.getFloatPref())); // loaded correctly

        esperandroPreferences.setIntegerPref(99);
        Assert.assertEquals(99, androidPreferences.getInt("integerPref", 0)); // saved correctly
        Assert.assertEquals(99, esperandroPreferences.getIntegerPref()); // loaded correctly

        esperandroPreferences.setLongPref(23);
        Assert.assertEquals(23, androidPreferences.getLong("longPref", 0l)); // saved correctly
        Assert.assertEquals(23, esperandroPreferences.getLongPref()); // loaded correctly

        esperandroPreferences.setStringPref("Some string");
        // saved correctly
        Assert.assertEquals("Some string", androidPreferences.getString("stringPref", "default string"));
        Assert.assertEquals("Some string", esperandroPreferences.getStringPref()); // loaded correctly

        List<String> strings = Arrays.asList("Some string", "other string");
        esperandroPreferences.setStringSetPref(new HashSet<String>(strings));
        Set<String> persistedStrings = androidPreferences.getStringSet("stringSetPref", new HashSet<String>());
        // saved correctly
        Assert.assertEquals(2, persistedStrings.size());
        Assert.assertTrue(persistedStrings.contains(strings.get(0)));
        Assert.assertTrue(persistedStrings.contains(strings.get(1)));
        // loaded correctly
        persistedStrings = esperandroPreferences.getStringSetPref();
        Assert.assertEquals(2, persistedStrings.size());
        Assert.assertTrue(persistedStrings.contains(strings.get(0)));
        Assert.assertTrue(persistedStrings.contains(strings.get(1)));
    }

    @Test
    public void remove() {
        esperandroPreferences.setBoolPref(true); // assert that save works
        esperandroPreferences.remove("boolPref");
        Assert.assertFalse(androidPreferences.getBoolean("boolPref", false)); // is now the default
        Assert.assertFalse(androidPreferences.contains("boolPref"));

        esperandroPreferences.setFloatPref(3.14f);
        esperandroPreferences.remove("floatPref");
        Assert.assertEquals(0, Float.compare(0f, androidPreferences.getFloat("floatPref", 0f)));
        Assert.assertFalse(androidPreferences.contains("floatPref"));

        esperandroPreferences.setIntegerPref(99);
        esperandroPreferences.remove("integerPref");
        Assert.assertEquals(0, androidPreferences.getInt("integerPref", 0));
        Assert.assertFalse(androidPreferences.contains("integerPref"));

        esperandroPreferences.setLongPref(23);
        esperandroPreferences.remove("longPref");
        Assert.assertEquals(0l, androidPreferences.getLong("longPref", 0l));
        Assert.assertFalse(androidPreferences.contains("longPref"));

        esperandroPreferences.setStringPref("Some string");
        esperandroPreferences.remove("stringPref");
        Assert.assertEquals("default string", androidPreferences.getString("stringPref", "default string"));
        Assert.assertFalse(androidPreferences.contains("stringPref"));

        List<String> strings = Arrays.asList("Some string", "other string");
        esperandroPreferences.setStringSetPref(new HashSet<String>(strings));
        esperandroPreferences.remove("stringSetPref");
        Set<String> persistedStrings = androidPreferences.getStringSet("stringSetPref", null);
        Assert.assertNull(persistedStrings);
        Assert.assertFalse(androidPreferences.contains("stringSetPref"));
    }
}
