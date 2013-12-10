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

import de.devland.esperandro.tests.EsperandroReflectionExample;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Basically checks that generation works when referencing a binary super interface instead of a local one.
 * Functionality is the same. When the compiler does not correctly generate the class there will be compile failures
 * because the implementation does not override all interface methods. Therefore only simple sanity checks are needed
 * here.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ReflectionTest {
    @Test
    public void testNotNull() {
        EsperandroReflectionExample preferences = Esperandro.getPreferences(EsperandroReflectionExample.class,
                Robolectric.application);
        Assert.assertNotNull(preferences);
    }

    @Test
    public void testSameObject() {
        EsperandroReflectionExample preferences1 = Esperandro.getPreferences(EsperandroReflectionExample.class,
                Robolectric.application);
        EsperandroReflectionExample preferences2 = Esperandro.getPreferences(EsperandroReflectionExample.class,
                Robolectric.application);
        Assert.assertNotNull(preferences1);
        Assert.assertNotNull(preferences2);
        Assert.assertEquals(preferences1, preferences2);
    }
}
