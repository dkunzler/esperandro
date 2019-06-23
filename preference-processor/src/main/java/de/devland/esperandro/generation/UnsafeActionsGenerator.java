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
import com.squareup.javapoet.TypeVariableName;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.UnsafeActions;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.base.preferences.PreferenceInterface;
import de.devland.esperandro.processor.PreferenceInformation;

public class UnsafeActionsGenerator {

    public static void createUnsafeActions(TypeSpec.Builder type, PreferenceInterface allPreferences, Cached cachedAnnotation) {
        TypeVariableName typeVariable = TypeVariableName.get("V");
        MethodSpec.Builder getValueBuilder = MethodSpec.methodBuilder("getValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addException(UnsafeActions.UnknownKeyException.class)
                .addTypeVariable(typeVariable)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(int.class, "prefId")
                .returns(typeVariable);

        getValueBuilder.addStatement("String prefKey = context.getString(prefId)");

        for (PreferenceInformation info : allPreferences) {
            // when getter is missing, generate one
            if (info.getter == null) {
                new GetterGenerator(null, null).createPrivate(type, info, cachedAnnotation);
            }
            getValueBuilder.beginControlFlow("if (prefKey.equals($S))", info.preferenceName);
            if (info.preferenceType.isPrimitive()) {
                // box
                getValueBuilder.addStatement("return (V) ($T) $L()", info.preferenceType.getObjectType(), info.preferenceName);
            } else {
                getValueBuilder.addStatement("return (V) $L()", info.preferenceName);
            }
            getValueBuilder.endControlFlow();
        }
        getValueBuilder.addStatement("throw new $T(prefKey)", UnsafeActions.UnknownKeyException.class);

        MethodSpec.Builder setValueBuilder = MethodSpec.methodBuilder("setValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addException(UnsafeActions.UnknownKeyException.class)
                .addTypeVariable(typeVariable)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(int.class, "prefId")
                .addParameter(typeVariable, "pref");

        setValueBuilder.addStatement("String prefKey = context.getString(prefId)");

        for (PreferenceInformation info : allPreferences) {
            // when setter is missing, generate one
            if (info.setter == null && info.commitSetter == null) {
                new PutterGenerator().createPrivate(type, info, cachedAnnotation);
            }

            setValueBuilder.beginControlFlow("if (prefKey.equals($S))", info.preferenceName);
            if (info.preferenceType.isPrimitive()) {
                // box
                setValueBuilder.addStatement("$L(($T) ($T)pref)", info.preferenceName, info.preferenceType.getType(), info.preferenceType.getObjectType());
            } else if (info.preferenceType.getTypeName().equals("Byte")) {
                // box, byte is not handled as primitive
                setValueBuilder.addStatement("$L(($T) (Byte)pref)", info.preferenceName, info.preferenceType.getType());
            } else {
                setValueBuilder.addStatement("$L(($T)pref)", info.preferenceName, info.preferenceType.getType());
            }
            setValueBuilder.addStatement("return");
            setValueBuilder.endControlFlow();
        }
        setValueBuilder.addStatement("throw new $T(prefKey)", UnsafeActions.UnknownKeyException.class);

        type.addSuperinterface(UnsafeActions.class)
                .addMethod(getValueBuilder.build())
                .addMethod(setValueBuilder.build());
    }
}
