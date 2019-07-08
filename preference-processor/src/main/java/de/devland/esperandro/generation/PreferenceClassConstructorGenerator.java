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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.SharedPreferenceMode;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.base.preferences.PreferenceInterface;

public class PreferenceClassConstructorGenerator {
    public static void createConstructor(TypeSpec.Builder type, PreferenceInterface preferenceInterface) {
        String preferencesName = preferenceInterface.getPreferenceAnnotation().name();
        SharedPreferenceMode mode = preferenceInterface.getPreferenceAnnotation().mode();
        Cached cacheAnnotation = preferenceInterface.getCacheAnnotation();
        boolean preferenceNamePresent = !preferencesName.isEmpty();

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
            type.addField(lruCache, "cache", Modifier.PRIVATE, Modifier.FINAL);

            constructor.addStatement("cache = new LruCache<$T, $T>(cacheSize)", String.class, Object.class);
        }

        type.addMethod(constructor.build());
    }

    public static void createDefaultConstructor(TypeSpec.Builder type, PreferenceInterface allPreferences) {
        Cached cacheAnnotation = allPreferences.getCacheAnnotation();
        if (cacheAnnotation != null) {
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("android.content", "Context"), "context");
            int cacheSize = cacheAnnotation.cacheSize();
            if (cacheAnnotation.autoSize()) {
                cacheSize = allPreferences.getAllPreferences().size();
            }
            constructor.addStatement("this(context, $L)", cacheSize);
            type.addMethod(constructor.build());
        }
    }
}
