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
import de.devland.esperandro.base.preferences.PreferenceInterface;
import de.devland.esperandro.base.preferences.TypeInformation;

public class UnsafeActionsGenerator {

    public static void createUnsafeActions(TypeSpec.Builder type, PreferenceInterface allPreferences) {
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

        for (String preferenceName : allPreferences.getAllPreferences()) {
            TypeInformation typeOfPreference = allPreferences.getTypeOfPreference(preferenceName);
            getValueBuilder.beginControlFlow("if (prefKey.equals($S))", preferenceName);
            if (typeOfPreference.isPrimitive()) {
                // box
                getValueBuilder.addStatement("return (V) ($T) $L()", typeOfPreference.getObjectType(), preferenceName);
            } else {
                getValueBuilder.addStatement("return (V) $L()", preferenceName);
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

        for (String preferenceName : allPreferences.getAllPreferences()) {
            TypeInformation typeOfPreference = allPreferences.getTypeOfPreference(preferenceName);

            setValueBuilder.beginControlFlow("if (prefKey.equals($S))", preferenceName);
            if (typeOfPreference.isPrimitive()) {
                // box
                setValueBuilder.addStatement("$L(($T) ($T)pref)", preferenceName, typeOfPreference.getType(), typeOfPreference.getObjectType());
            } else if (typeOfPreference.getTypeName().equals("Byte")) {
                // box, byte is not handled as primitive
                setValueBuilder.addStatement("$L(($T) (Byte)pref)", preferenceName, typeOfPreference.getType());
            } else {
                setValueBuilder.addStatement("$L(($T)pref)", preferenceName, typeOfPreference.getType());
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
