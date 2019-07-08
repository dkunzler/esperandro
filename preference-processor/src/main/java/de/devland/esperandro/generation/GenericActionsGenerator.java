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
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.Utils;
import de.devland.esperandro.base.preferences.EsperandroType;
import de.devland.esperandro.base.preferences.PreferenceInterface;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class GenericActionsGenerator {

    public static void createGenericActions(TypeSpec.Builder type, PreferenceInterface allPreferences) {
        boolean caching = allPreferences.getCacheAnnotation() != null;

        MethodSpec.Builder get = MethodSpec.methodBuilder("get")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("android.content", "SharedPreferences"))
                .addStatement("return preferences");

        MethodSpec.Builder contains = MethodSpec.methodBuilder("contains")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addParameter(String.class, "key")
                .addStatement("return preferences.contains(key)");

        MethodSpec.Builder remove = MethodSpec.methodBuilder("remove")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(String.class, "key")
                .addStatement("preferences.edit().remove(key).$L", PreferenceEditorCommitStyle.APPLY.getStatementPart());

        MethodSpec.Builder registerListener = MethodSpec.methodBuilder("registerOnChangeListener")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.content", "SharedPreferences.OnSharedPreferenceChangeListener"), "listener")
                .addStatement("preferences.registerOnSharedPreferenceChangeListener(listener)");

        MethodSpec.Builder unregisterListener = MethodSpec.methodBuilder("unregisterOnChangeListener")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.content", "SharedPreferences.OnSharedPreferenceChangeListener"), "listener")
                .addStatement("preferences.unregisterOnSharedPreferenceChangeListener(listener)");

        MethodSpec.Builder clear = MethodSpec.methodBuilder("clearAll")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("preferences.edit().clear().$L", PreferenceEditorCommitStyle.APPLY.getStatementPart());


        MethodSpec.Builder resetCache = MethodSpec.methodBuilder("resetCache")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("cache.evictAll()");

        MethodSpec clearDefined = createClearDefinedMethod(allPreferences, caching, remove, clear);

        MethodSpec initDefaults = createInitDefaultsMethod(allPreferences);


        type.addSuperinterface(SharedPreferenceActions.class)
                .addMethod(get.build())
                .addMethod(contains.build())
                .addMethod(remove.build())
                .addMethod(registerListener.build())
                .addMethod(unregisterListener.build())
                .addMethod(clear.build())
                .addMethod(clearDefined)
                .addMethod(initDefaults);

        if (caching) {
            type.addMethod(resetCache.build());
        }
    }

    private static MethodSpec createInitDefaultsMethod(PreferenceInterface allPreferences) {
        MethodSpec.Builder initDefaultsBuilder = MethodSpec.methodBuilder("initDefaults")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("SharedPreferences.Editor editor = preferences.edit()");

        for (String preferenceName : allPreferences.getAllPreferences()) {
            String methodSuffix = Utils.getMethodSuffix(allPreferences.getTypeOfPreference(preferenceName).getEsperandroType());
            String value = "this." + preferenceName + "()";
            if (allPreferences.getTypeOfPreference(preferenceName).getEsperandroType() == EsperandroType.OBJECT) {
                value = "Esperandro.getSerializer().serialize(" + value + ")";
            }

            initDefaultsBuilder.addStatement("editor.put$L($S, $L)", methodSuffix, preferenceName, value);
        }

        initDefaultsBuilder.addStatement("editor.commit()");
        return initDefaultsBuilder.build();
    }

    private static MethodSpec createClearDefinedMethod(PreferenceInterface preferenceInterface, boolean caching, MethodSpec.Builder remove, MethodSpec.Builder clear) {
        MethodSpec.Builder clearDefinedBuilder = MethodSpec.methodBuilder("clearPreferences")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("SharedPreferences.Editor editor = preferences.edit()");


        for (String preferenceName : preferenceInterface.getAllPreferences()) {
            clearDefinedBuilder.addStatement("editor.remove($S)", preferenceName);
        }

        if (caching) {
            remove.addStatement("cache.remove(key)");
            clear.addStatement("cache.evictAll()");
            for (String preferenceName : preferenceInterface.getAllPreferences()) {
                clearDefinedBuilder.addStatement("cache.remove($S)", preferenceName);
            }
        }

        return clearDefinedBuilder
                .addStatement("editor.$L", PreferenceEditorCommitStyle.APPLY.getStatementPart())
                .build();
    }
}
