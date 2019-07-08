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

package de.devland.esperandro.generation;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.Serializable;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.base.preferences.PreferenceInterface;
import java8.util.stream.StreamSupport;

public class ContainerClassGenerator {
    public static void createGenericClassImplementations(TypeSpec.Builder type, PreferenceInterface preferenceInterface) {
        StreamSupport.stream(preferenceInterface.getAllPreferences())
                .filter(name -> preferenceInterface.getTypeOfPreference(name).isGeneric())
                .forEach(name -> {
                    FieldSpec serialVersionUid = FieldSpec.builder(TypeName.LONG, "serialVersionUID", Modifier.PRIVATE)
                            .initializer("1L")
                            .build();
                    TypeSpec innerGenericType = TypeSpec.classBuilder(createClassNameForPreference(name))
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .addSuperinterface(Serializable.class)
                            .addField(serialVersionUid)
                            .addField(preferenceInterface.getTypeOfPreference(name).getType(), "value", Modifier.PUBLIC)
                            .build();

                    type.addType(innerGenericType);
                });
    }

    private static String createClassNameForPreference(String valueName) {
        return valueName.substring(0, 1).toUpperCase() + valueName.substring(1);
    }
}
