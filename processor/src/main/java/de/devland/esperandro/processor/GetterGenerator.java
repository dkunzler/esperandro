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
package de.devland.esperandro.processor;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import de.devland.esperandro.Esperandro;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.experimental.Cached;
import de.devland.esperandro.serialization.Serializer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetterGenerator {

    private Warner warner;

    public GetterGenerator(Warner warner) {
        this.warner = warner;
    }

    private MethodSpec.Builder initGetter(String valueName, PreferenceTypeInformation preferenceTypeInformation, boolean runtimeDefault) {
        MethodSpec.Builder getterBuilder;
        if (runtimeDefault) {
            getterBuilder = MethodSpec.methodBuilder(valueName + Constants.RUNTIME_DEFAULT_SUFFIX)
                    .addParameter(preferenceTypeInformation.getType(), "defaultValue");
        } else {
            getterBuilder = MethodSpec.methodBuilder(valueName);
        }
        getterBuilder.addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(preferenceTypeInformation.getType());
        return getterBuilder;
    }

    private String getDefaultValue(Default defaultAnnotation, PreferenceType preferenceType, Element element) {
        boolean allDefaults = false;
        boolean hasDefaultAnnotation = defaultAnnotation != null;
        if (hasDefaultAnnotation) {
            allDefaults = hasAllDefaults(defaultAnnotation);
        }
        String defaultValue = "";
        switch (preferenceType) {
            case INT:
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofInt() == Default.intDefault) {
                    warner.emitMissingDefaultWarning("int", element);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofInt()) : String.valueOf
                        (Default.intDefault);
                break;
            case LONG:
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofLong() == Default.longDefault) {
                    warner.emitMissingDefaultWarning("long", element);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofLong()) : String.valueOf
                        (Default.longDefault);
                defaultValue += "l";
                break;
            case FLOAT:
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofFloat() == Default.floatDefault) {
                    warner.emitMissingDefaultWarning("float", element);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofFloat()) : String.valueOf
                        (Default.floatDefault);
                defaultValue += "f";
                break;
            case BOOLEAN:
                //noinspection PointlessBooleanExpression
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofBoolean() == Default.booleanDefault) {
                    warner.emitMissingDefaultWarning("boolean", element);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofBoolean()) : String.valueOf
                        (Default.booleanDefault);
                break;
            case STRING:
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofString().equals(Default
                        .stringDefault)) {
                    warner.emitMissingDefaultWarning("String", element);
                }
                defaultValue = (hasDefaultAnnotation ? ("\"" + defaultAnnotation.ofString() + "\"") : ("\"" + Default
                        .stringDefault + "\""));
                break;
            case STRINGSET:
                if (hasDefaultAnnotation) {
                    warner.emitWarning("No default for Set<String> preferences allowed.", element);
                }
                defaultValue = "null";
                break;
            case OBJECT:
                if (hasDefaultAnnotation) {
                    warner.emitWarning("No default for Object preferences allowed.", element);
                }
                defaultValue = "null";
                break;
            case UNKNOWN:
                break;
        }

        return defaultValue;
    }


    private boolean hasAllDefaults(Default defaultAnnotation) {
        //noinspection PointlessBooleanExpression
        boolean hasAllDefaults = defaultAnnotation.ofBoolean() == Default.booleanDefault;
        hasAllDefaults &= defaultAnnotation.ofInt() == Default.intDefault;
        hasAllDefaults &= defaultAnnotation.ofFloat() == Default.floatDefault;
        hasAllDefaults &= defaultAnnotation.ofLong() == Default.longDefault;
        hasAllDefaults &= defaultAnnotation.ofString().equals(Default.stringDefault);

        return hasAllDefaults;
    }


    public void create(TypeSpec.Builder type, PreferenceInformation info, Cached cachedAnnotation, boolean runtimeDefault) {
        MethodSpec.Builder getterBuilder = initGetter(info.preferenceName, info.typeInformation, runtimeDefault);
        Element element = runtimeDefault ? info.runtimeDefaultGetterElement : info.getterElement;

        if (cachedAnnotation != null) {
            getterBuilder.addStatement("$T __result = ($T) cache.get($S)", info.typeInformation.getObjectType(), info.typeInformation.getObjectType(), info.preferenceName);
            getterBuilder.beginControlFlow("if (__result == null)");
        } else {
            getterBuilder.addStatement("$T __result", info.typeInformation.getType());
        }

        if (runtimeDefault) {
            getterBuilder.beginControlFlow("if (preferences.contains($S))", info.preferenceName);
        }

        String statementPattern = "preferences.get%s(\"%s\", %s)";
        String methodSuffix = Utils.getMethodSuffix(info.typeInformation.getPreferenceType());
        String defaultValue = getDefaultValue(info.defaultAnnotation, info.typeInformation.getPreferenceType(), element);
        if (info.typeInformation.getPreferenceType() == PreferenceType.OBJECT) {
            getterBuilder.addStatement("$T __serializer = $T.getSerializer()", Serializer.class, Esperandro.class);
            if (info.typeInformation.isGeneric()) {
                String genericClassName = Utils.createClassNameForPreference(info.preferenceName);
                String statement = String.format(statementPattern, methodSuffix, info.preferenceName, defaultValue);
                getterBuilder.addStatement("$L __container = __serializer.deserialize($L, $L.class)", genericClassName, statement, genericClassName);
                getterBuilder.addStatement("$L __value = null", info.typeInformation.getTypeName());
                getterBuilder.beginControlFlow("if (__container != null)");
                getterBuilder.addStatement("__value = __container.value");
                getterBuilder.endControlFlow();
                statementPattern = "__value";
            } else {
                statementPattern = String.format("__serializer.deserialize(%s, %s.class)",
                        statementPattern, info.typeInformation.getTypeName());
            }
        }

        // TODO
//        if (info.runtimeDefaultGetterElement != null && runtimeDefault) {
//            warner.emitWarning("Pointless @Default Annotation", element);
//        }
        String statement = String.format(statementPattern, methodSuffix, info.preferenceName, defaultValue);
        getterBuilder.addStatement("__result = $L", statement);
        if (runtimeDefault) {
            getterBuilder.nextControlFlow("else")
                    .addStatement("__result = defaultValue")
                    .endControlFlow();
        }

        if (cachedAnnotation != null) {
            getterBuilder.beginControlFlow("if (__result != null)");
            getterBuilder.addStatement("cache.put($S, __result)", info.preferenceName);
            getterBuilder.endControlFlow();
            getterBuilder.endControlFlow();
        }
        getterBuilder.addStatement("return __result");
        type.addMethod(getterBuilder.build());
    }
}
