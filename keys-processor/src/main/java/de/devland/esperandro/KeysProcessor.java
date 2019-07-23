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

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.util.Collections;
import java.util.List;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import de.devland.esperandro.base.MethodAnalyzer;
import de.devland.esperandro.base.Utils;
import de.devland.esperandro.base.preferences.PreferenceInterface;
import de.devland.esperandro.base.processing.AbstractEsperandroProcessor;

@SupportedAnnotationTypes("de.devland.esperandro.annotations.SharedPreferences")
public class KeysProcessor extends AbstractEsperandroProcessor {
    @Override
    protected List<? extends MethodAnalyzer> getMethodAnalyzers() {
        return Collections.singletonList(new NameMethodAnalyzer());
    }

    @Override
    protected void generate(Element currentElement, PreferenceInterface preferenceInterface) throws Exception {
        TypeSpec.Builder result;

        String typeName = Utils.classNameFromInterface(currentElement) + Constants.SUFFIX_KEYS;
        result = TypeSpec.classBuilder(typeName);

        if (Utils.isPublic(currentElement)) {
            result.addModifiers(Modifier.PUBLIC);
        }

        for (String preferenceName : preferenceInterface.getAllPreferences()) {
            result.addField(generateField(preferenceName));
        }

        String packageName = Utils.packageNameFromInterface(currentElement);
        JavaFile javaFile = JavaFile.builder(packageName, result.build())
                .build();
        javaFile.writeTo(processingEnv.getFiler());
    }

    private static FieldSpec generateField(String key) {
        FieldSpec.Builder builder = FieldSpec.builder(String.class, key, Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC);
        builder.initializer("$S", key);
        return builder.build();
    }
}
