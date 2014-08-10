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

import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;

import java.util.ArrayList;

@SharedPreferences
public interface EsperandroSerializationExample {

    void containerValue(Container container);

    Container containerValue();

    void stringList(ArrayList<String> stringList);

    ArrayList<String> stringList();

    void containerList(ArrayList<Container> containerList);

    ArrayList<Container> containerList();

    void containerListObject(ContainerListObject containerListObject);

    ContainerListObject containerListObject();

    boolean containerValueSync(Container container);

    Container containerValueSync();

    public static class ContainerListObject extends ArrayList<Container> {
        public ContainerListObject() {}
    }

}
