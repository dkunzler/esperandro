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

import java.util.ArrayList;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;
import de.devland.esperandro.tests.model.ContainerListObject;

@SharedPreferences
public interface EsperandroSerializationExample extends SharedPreferenceActions {

    void setContainerValue(Container container);

    Container getContainerValue();

    void setStringList(ArrayList<String> stringList);

    ArrayList<String> getStringList();

    void setContainerList(ArrayList<Container> containerList);

    ArrayList<Container> getContainerList();

    ArrayList<Container> getContainerList(ArrayList<Container> runtimeDefault);

    void setContainerListObject(ContainerListObject containerListObject);

    ContainerListObject getContainerListObject();

    boolean setContainerValueSync(Container container);

    Container getContainerValueSync();

    void setContainerDefault(Container container);

    @Default(ofStatement = "new de.devland.esperandro.tests.model.Container()")
    Container getContainerDefault();

    byte getByteValue();
    void setByteValue(byte byteValue);

    char getCharValue();
    void setCharValue(char charValue);

}
