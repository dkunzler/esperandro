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

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.base.preferences.EsperandroType;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.TypeInformation;
import de.devland.esperandro.base.processing.Environment;

public class CollectionActionGenerator implements MethodGenerator {

    private final String action;

    public CollectionActionGenerator(String action) {
        this.action = action;
    }

    @Override
    public void generateMethod(TypeSpec.Builder type, MethodInformation methodInformation, Cached cacheAnnotation) {
        String prefName = methodInformation.associatedPreference;
        TypeInformation preferenceType = Environment.currentPreferenceInterface.getTypeOfPreference(prefName);
        MethodSpec.Builder adder = MethodSpec.methodBuilder(methodInformation.getMethodName())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(methodInformation.parameterType.getType(), "value")
                .addStatement("$T __pref = this.$L()", preferenceType.getObjectType(), prefName);

        if (preferenceType.getEsperandroType() == EsperandroType.STRINGSET) {
            // special handling for Set<String> since you shouldn't edit the returned object itself as per
            // official documentation
            adder.addStatement("__pref = new java.util.HashSet<String>(__pref)");
        }
        adder.addStatement("__pref.$L(value)", action)
                .addStatement("this.$L(__pref)", prefName);
        type.addMethod(adder.build());
    }
}
