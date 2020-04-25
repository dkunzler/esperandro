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
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import de.devland.esperandro.Constants;
import de.devland.esperandro.Esperandro;
import de.devland.esperandro.Utils;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.base.preferences.EsperandroType;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.TypeInformation;
import de.devland.esperandro.base.processing.Environment;
import de.devland.esperandro.base.processing.ProcessingMessager;
import de.devland.esperandro.serialization.Serializer;

public class GetterGenerator implements MethodGenerator {

    private final boolean runtimeDefault;

    public GetterGenerator(boolean runtimeDefault) {
        this.runtimeDefault = runtimeDefault;
    }

    @Override
    public void generateMethod(TypeSpec.Builder type, MethodInformation methodInformation, Cached cacheAnnotation) {
        MethodSpec.Builder getterBuilder = initGetter(methodInformation.associatedPreference, methodInformation.returnType, runtimeDefault, methodInformation.isInternal());
        Element element = methodInformation.element != null ? methodInformation.element : Environment.currentElement;
        String defaultValue = getDefaultValue(methodInformation.getAnnotation(Default.class), methodInformation.returnType, element);
        createInternal(type, element, methodInformation.returnType, cacheAnnotation, runtimeDefault, defaultValue, getterBuilder, methodInformation);
    }

    private MethodSpec.Builder initGetter(String valueName, TypeInformation typeInformation, boolean runtimeDefault, boolean internal) {
        String methodName = Constants.PREFIX_GET + Utils.upperCaseFirstLetter(valueName);
        MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(methodName);
        if (runtimeDefault) {
            getterBuilder.addParameter(typeInformation.getType(), "defaultValue");
        }
        getterBuilder.addModifiers(internal ? Modifier.PRIVATE : Modifier.PUBLIC)
                .returns(typeInformation.getType());

        if (!internal) {
            getterBuilder.addAnnotation(Override.class);
        }

        return getterBuilder;
    }

    private void createInternal(TypeSpec.Builder type, Element element, TypeInformation typeInfo, Cached cachedAnnotation,
                                boolean runtimeDefault, String defaultValue, MethodSpec.Builder getterBuilder,
                                MethodInformation methodInformation) {

        if (cachedAnnotation != null) {
            getterBuilder.addStatement("$T __result = ($T) cache.get($S)", typeInfo.getObjectType(), typeInfo.getObjectType(), methodInformation.associatedPreference);
            getterBuilder.beginControlFlow("if (__result == null)");
        } else {
            getterBuilder.addStatement("$T __result", typeInfo.getType());
        }

        if (runtimeDefault) {
            getterBuilder.beginControlFlow("if (preferences.contains($S))", methodInformation.associatedPreference);
        }

        String statementPattern = "preferences.get%s(\"%s\", %s)";
        if (typeInfo.getEsperandroType() == EsperandroType.CHAR) {
            statementPattern = "(char) " + statementPattern;
        }
        if (typeInfo.getEsperandroType() == EsperandroType.BYTE) {
            statementPattern = "(byte) " + statementPattern;
        }
        String methodSuffix = Utils.getMethodSuffix(typeInfo.getEsperandroType());
        Default defaultAnnotation = methodInformation.getAnnotation(Default.class);
        if (typeInfo.getEsperandroType() == EsperandroType.OBJECT) {
            getterBuilder.addStatement("$T __serializer = $T.getSerializer()", Serializer.class, Esperandro.class);
            String statement = String.format(statementPattern, methodSuffix, methodInformation.associatedPreference, defaultValue);
            getterBuilder.addStatement("$T __prefValue = $L", TypeName.get(String.class), statement);
            getterBuilder.addStatement("$L __value = null", typeInfo.getTypeName());
            if (typeInfo.isGeneric()) {
                String genericClassName = Utils.createClassNameForPreference(methodInformation.associatedPreference);
                getterBuilder.addStatement("$L __container = __serializer.deserialize(__prefValue, $L.class)", genericClassName, genericClassName);
                getterBuilder.beginControlFlow("if (__container != null)");
                getterBuilder.addStatement("__value = __container.value");
                getterBuilder.endControlFlow();
                statementPattern = "__value";
            } else {
                getterBuilder.addStatement("__value = __serializer.deserialize(__prefValue, $L.class)", typeInfo.getTypeName());
                statementPattern = "__value";
            }
        }

        if (defaultAnnotation != null && runtimeDefault) {
            ProcessingMessager.get().emitError("@Default annotation not supported when using runtime defaults", element);
        }
        String statement = String.format(statementPattern, methodSuffix, methodInformation.associatedPreference, defaultValue);
        getterBuilder.addStatement("__result = $L", statement);
        if (runtimeDefault) {
            getterBuilder.nextControlFlow("else")
                    .addStatement("__result = defaultValue")
                    .endControlFlow();
        }

        if (cachedAnnotation != null) {
            getterBuilder.beginControlFlow("if (__result != null)");
            getterBuilder.addStatement("cache.put($S, __result)", methodInformation.associatedPreference);
            getterBuilder.endControlFlow();
            getterBuilder.endControlFlow();
        }
        getterBuilder.addStatement("return __result");
        type.addMethod(getterBuilder.build());
    }

    private String getDefaultValue(Default defaultAnnotation, TypeInformation preferenceType, Element element) {
        boolean defaultAnnotationHasValue = false;
        boolean hasDefaultAnnotation = defaultAnnotation != null;
        if (hasDefaultAnnotation) {
            defaultAnnotationHasValue = !hasAllDefaults(defaultAnnotation);
        }
        String defaultValue = "";
        // if everything else is default use statement if set
        if (!defaultAnnotationHasValue && hasDefaultAnnotation && !defaultAnnotation.ofStatement().equals(Default.stringDefault)) {
            defaultValue = defaultAnnotation.ofStatement();
            if (preferenceType.getEsperandroType() == EsperandroType.OBJECT) {
                defaultValue = "__serializer.serialize(" + defaultValue + ")";
            }
        } else {
            switch (preferenceType.getEsperandroType()) {
                case INT:
                    if (hasDefaultAnnotation && defaultAnnotationHasValue && defaultAnnotation.ofInt() == Default.intDefault) {
                        ProcessingMessager.get().emitMissingDefaultWarning("int", element);
                    }
                    defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofInt()) : String.valueOf
                            (Default.intDefault);
                    break;
                case LONG:
                    if (hasDefaultAnnotation && defaultAnnotationHasValue && defaultAnnotation.ofLong() == Default.longDefault) {
                        ProcessingMessager.get().emitMissingDefaultWarning("long", element);
                    }
                    defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofLong()) : String.valueOf
                            (Default.longDefault);
                    defaultValue += "l";
                    break;
                case FLOAT:
                    if (hasDefaultAnnotation && defaultAnnotationHasValue && defaultAnnotation.ofFloat() == Default.floatDefault) {
                        ProcessingMessager.get().emitMissingDefaultWarning("float", element);
                    }
                    defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofFloat()) : String.valueOf
                            (Default.floatDefault);
                    defaultValue += "f";
                    break;
                case BOOLEAN:
                    if (hasDefaultAnnotation && defaultAnnotationHasValue && defaultAnnotation.ofBoolean() == Default.booleanDefault) {
                        ProcessingMessager.get().emitMissingDefaultWarning("boolean", element);
                    }
                    defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofBoolean()) : String.valueOf
                            (Default.booleanDefault);
                    break;
                case BYTE:
                    if (hasDefaultAnnotation && defaultAnnotationHasValue && defaultAnnotation.ofByte() == Default.byteDefault) {
                        ProcessingMessager.get().emitMissingDefaultWarning("byte", element);
                    }
                    defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofByte()) : String.valueOf
                            (Default.byteDefault);
                    break;
                case CHAR:
                    if (hasDefaultAnnotation && defaultAnnotationHasValue && defaultAnnotation.ofChar() == Default.charDefault) {
                        ProcessingMessager.get().emitMissingDefaultWarning("char", element);
                    }
                    defaultValue = hasDefaultAnnotation ? String.valueOf((int) defaultAnnotation.ofChar()) : String.valueOf
                            ((int) Default.charDefault);
                    break;
                case STRING:
                    if (hasDefaultAnnotation && defaultAnnotationHasValue && defaultAnnotation.ofString().equals(Default
                            .stringDefault)) {
                        ProcessingMessager.get().emitMissingDefaultWarning("String", element);
                    }
                    defaultValue = (hasDefaultAnnotation ? ("\"" + defaultAnnotation.ofString() + "\"") : ("\"" + Default
                            .stringDefault + "\""));
                    break;
                case STRINGSET:
                case OBJECT:
                    defaultValue = "null";
                    break;
                case UNKNOWN:
                    break;
            }
        }

        return defaultValue;
    }

    private boolean hasAllDefaults(Default defaultAnnotation) {
        boolean hasAllDefaults = defaultAnnotation.ofBoolean() == Default.booleanDefault;
        hasAllDefaults &= defaultAnnotation.ofInt() == Default.intDefault;
        hasAllDefaults &= defaultAnnotation.ofFloat() == Default.floatDefault;
        hasAllDefaults &= defaultAnnotation.ofLong() == Default.longDefault;
        hasAllDefaults &= defaultAnnotation.ofChar() == Default.charDefault;
        hasAllDefaults &= defaultAnnotation.ofByte() == Default.byteDefault;
        hasAllDefaults &= defaultAnnotation.ofString().equals(Default.stringDefault);

        return hasAllDefaults;
    }
}
