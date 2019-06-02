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

package de.devland.esperandro.processor.generation;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.processor.Constants;
import de.devland.esperandro.processor.PreferenceInformation;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class ContainsGenerator {

    public static void generate(TypeSpec.Builder type, PreferenceInformation info) {
        MethodSpec.Builder contains = MethodSpec.methodBuilder(info.preferenceName + Constants.SUFFIX_CONTAINS)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addParameter(info.remover.parameterType.getType(), "value")
                .addStatement("$T __pref = this.$L()", info.preferenceType.getObjectType(), info.preferenceName)
                .addStatement("return __pref.contains(value)");
        type.addMethod(contains.build());
    }
}
