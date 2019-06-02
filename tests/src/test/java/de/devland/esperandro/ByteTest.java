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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.devland.esperandro.serialization.JacksonSerializer;
import de.devland.esperandro.tests.EsperandroArrayExample;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ByteTest {

    private EsperandroArrayExample esperandroPreferences;

    @Before
    public void setup() {
        esperandroPreferences = Esperandro.getPreferences(EsperandroArrayExample.class, RuntimeEnvironment.application);
        Esperandro.setSerializer(new JacksonSerializer());
    }

    @After
    public void tearDown() {
        esperandroPreferences.clear();
    }

    @Test
    public void testPutGetByteArray() {
        Assert.assertEquals(0, esperandroPreferences.secretKey$Default(new byte[0]).length);

        esperandroPreferences.secretKey(new byte[]{1, 2, 3});

        Assert.assertEquals(3, esperandroPreferences.secretKey$Default(new byte[0]).length);

    }
}
