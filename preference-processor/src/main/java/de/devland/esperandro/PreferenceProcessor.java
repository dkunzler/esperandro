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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.base.MethodAnalyzer;
import de.devland.esperandro.base.preferences.PreferenceInterface;
import de.devland.esperandro.base.processing.AbstractEsperandroProcessor;
import de.devland.esperandro.generation.GenericActionsGenerator;
import de.devland.esperandro.generation.UnsafeActionsGenerator;

public class PreferenceProcessor extends AbstractEsperandroProcessor {
    @Override
    protected List<MethodAnalyzer> getMethodAnalyzers() {
        return null;
    }

    @Override
    protected void generate(Element currentElement, PreferenceInterface preferenceInterface) throws Exception {
        Cached cacheAnnotation = preferenceInterface.getCacheAnnotation();
        TypeSpec.Builder type = initImplementation(currentElement, cacheAnnotation);
        createMethods(type, preferenceInterface, cacheAnnotation);
        GenericActionsGenerator.createGenericActions(type, preferenceInterface, cacheAnnotation != null);
        UnsafeActionsGenerator.createUnsafeActions(type, preferenceInterface, cacheAnnotation);
        createGenericClassImplementations(type, preferenceInterface);
        createDefaultConstructor(type, preferenceInterface, cacheAnnotation);
        finish(currentElement, preferenceInterface, type);
    }

    private TypeSpec.Builder initImplementation(Element interfaze, Cached cacheAnnotation) {
        TypeSpec.Builder result;
        SharedPreferences prefAnnotation = interfaze.getAnnotation(SharedPreferences.class);
        String preferencesName = prefAnnotation.name();
        SharedPreferenceMode mode = prefAnnotation.mode();
        if (cacheAnnotation != null && preferencesName.equals("")) {
            messager.emitWarning("Caching should not be used on default SharedPreferences. This is not officially supported.", interfaze);
        }

        try {
            boolean preferenceNamePresent = !preferencesName.equals("");
            String typeName = Utils.classNameFromInterface(interfaze) + Constants.IMPLEMENTATION_SUFFIX;
            result = TypeSpec.classBuilder(typeName)
                    .addSuperinterface(TypeName.get(interfaze.asType()))
                    .addField(ClassName.get("android.content", "SharedPreferences"), "preferences", Modifier.PRIVATE, Modifier.FINAL);
            if (Utils.isPublic(interfaze)) {
                result.addModifiers(Modifier.PUBLIC);
            }
            if (cacheAnnotation != null) {
                result.addSuperinterface(CacheActions.class);
            }
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("android.content", "Context"), "context");
            if (preferenceNamePresent) {
                constructor.addStatement("this.preferences = context.getSharedPreferences($S, $L)", preferencesName,
                        mode.getSharedPreferenceModeStatement());
            } else {
                constructor.addStatement("this.preferences = $T.getDefaultSharedPreferences(context)", ClassName.get("android.preference", "PreferenceManager"));
            }

            if (cacheAnnotation != null) {
                constructor.addParameter(TypeName.INT, "cacheSize");
                ClassName cacheClass;
                switch (cacheAnnotation.cacheVersion()) {
                    case SUPPORT_V4:
                        cacheClass = ClassName.get("android.support.v4.util", "LruCache");
                        break;
                    case ANDROID_X:
                        cacheClass = ClassName.get("androidx.collection", "LruCache");
                        break;
                    case FRAMEWORK:
                    default:
                        cacheClass = ClassName.get("android.util", "LruCache");
                        break;
                }
                ParameterizedTypeName lruCache = ParameterizedTypeName.get(
                        cacheClass,
                        ClassName.get(String.class),
                        ClassName.get(Object.class));
                result.addField(lruCache, "cache", Modifier.PRIVATE, Modifier.FINAL);

                constructor.addStatement("cache = new LruCache<$T, $T>(cacheSize)", String.class, Object.class);
            }

            result.addMethod(constructor.build());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
