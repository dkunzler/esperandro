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

import de.devland.esperandro.tests.EsperandroSerializationExample;
import de.devland.esperandro.tests.model.Container;
import de.devland.esperandro.tests.model.ContainerListObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public abstract class SerializationBaseTest {

    EsperandroSerializationExample esperandroPreferences;

    @Before
    public void setup() {
        esperandroPreferences = Esperandro.getPreferences(EsperandroSerializationExample.class, RuntimeEnvironment.application);
        setSerializer();
        //Esperandro.setSerializer(new JacksonSerializer());
    }

    @After
    public void tearDown() {
        esperandroPreferences.clear();
    }

    protected abstract void setSerializer();

    @Test
    public void simpleSerialization() {
        Assert.assertNull(esperandroPreferences.containerValue());
        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";

        esperandroPreferences.containerValue(container);

        Container savedContainer = esperandroPreferences.containerValue();
        Assert.assertNotNull(savedContainer);
        Assert.assertEquals(container, savedContainer);
    }


    @Test
    public void serializedList() {
        Assert.assertNull(esperandroPreferences.containerList());
        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";
        ArrayList<Container> list = new ArrayList<Container>();
        list.add(container);

        esperandroPreferences.containerList(list);

        ArrayList<Container> savedContainerList = esperandroPreferences.containerList();
        Assert.assertNotNull(savedContainerList);
        Assert.assertEquals(1, savedContainerList.size());
        Container savedContainer = savedContainerList.get(0);
        Assert.assertEquals(container, savedContainer);
    }

    @Test
    public void serializedListObject() {
        Assert.assertNull(esperandroPreferences.containerListObject());
        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";
        ContainerListObject list = new ContainerListObject();
        list.add(container);

        esperandroPreferences.containerListObject(list);

        ContainerListObject savedContainerListObject = esperandroPreferences.containerListObject();
        Assert.assertNotNull(savedContainerListObject);
        Assert.assertEquals(1, savedContainerListObject.size());
        Container savedContainer = savedContainerListObject.get(0);
        Assert.assertEquals(container, savedContainer);
    }

    @Test
    public void testContainerDefault() {
        Assert.assertNotNull(esperandroPreferences.containerDefault());
        Assert.assertEquals(0, esperandroPreferences.containerDefault().anotherValue);
        Assert.assertNull(esperandroPreferences.containerDefault().value);
    }

}
