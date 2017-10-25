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
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import de.devland.esperandro.Esperandro;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.experimental.Cached;
import de.devland.esperandro.processor.*;
import de.devland.esperandro.serialization.Serializer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

public class GetterGenerator {

    private Warner warner;
    private ProcessingEnvironment processingEnv;

    public GetterGenerator(Warner warner, ProcessingEnvironment processingEnv) {
        this.warner = warner;
        this.processingEnv = processingEnv;
    }

    private MethodSpec.Builder initGetter(String valueName, PreferenceTypeInformation preferenceTypeInformation, boolean runtimeDefault, boolean internal) {
        MethodSpec.Builder getterBuilder;
        if (runtimeDefault) {
            getterBuilder = MethodSpec.methodBuilder(valueName + Constants.SUFFIX_DEFAULT)
                    .addParameter(preferenceTypeInformation.getType(), "defaultValue");
        } else {
            getterBuilder = MethodSpec.methodBuilder(valueName);
        }
        getterBuilder.addModifiers(internal ? Modifier.PRIVATE : Modifier.PUBLIC)
                .returns(preferenceTypeInformation.getType());

        if (!internal) {
            getterBuilder.addAnnotation(Override.class);
        }

        return getterBuilder;
    }

    private String getDefaultValue(Default defaultAnnotation, PreferenceTypeInformation preferenceType, Element element) {
        boolean allDefaults = false;
        boolean hasDefaultAnnotation = defaultAnnotation != null;
        if (hasDefaultAnnotation) {
            allDefaults = hasAllDefaults(defaultAnnotation);
        }
        String defaultValue = "";
        switch (preferenceType.getPreferenceType()) {
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
                if (hasDefaultAnnotation && getOfClassDefault(defaultAnnotation).equals(getDefaultType())) {
                    warner.emitMissingDefaultWarning("Set<String>", element);
                    defaultValue = "null";
                } else if (hasDefaultAnnotation) {
                    defaultValue = "new " + getOfClassDefault(defaultAnnotation).toString() + "()";
                } else {
                    defaultValue = "null";
                }
                break;
            case OBJECT:
                if (hasDefaultAnnotation && getOfClassDefault(defaultAnnotation).equals(getDefaultType())) {
                    warner.emitMissingDefaultWarning(preferenceType.getTypeName(), element);
                    defaultValue = "null";
                } else {
                    defaultValue = "null";
                }
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
        hasAllDefaults &= getOfClassDefault(defaultAnnotation).equals(getDefaultType());

        return hasAllDefaults;
    }


    public void create(TypeSpec.Builder type, PreferenceInformation info, Cached cachedAnnotation, boolean runtimeDefault) {
        MethodSpec.Builder getterBuilder = initGetter(info.preferenceName, info.preferenceType, runtimeDefault, false);
        MethodInformation methodInformation = runtimeDefault ? info.runtimeDefaultGetter : info.getter;
        Element element = methodInformation.element;
        String defaultValue = getDefaultValue(runtimeDefault ? info.runtimeDefaultGetter.defaultAnnotation : info.getter.defaultAnnotation, info.preferenceType, element);
        createInternal(type, info, cachedAnnotation, runtimeDefault, defaultValue, getterBuilder, methodInformation);
    }

    void createPrivate(TypeSpec.Builder type, PreferenceInformation info, Cached cachedAnnotation) {
        MethodSpec.Builder getterBuilder = initGetter(info.preferenceName, info.preferenceType, false, true);
        String defaultValue = getDefaultValue(null, info.preferenceType, null);
        MethodInformation dummy = new MethodInformation(null, null, null, info.preferenceType, null);
        createInternal(type, info, cachedAnnotation, false, defaultValue, getterBuilder, dummy);
    }

    private void createInternal(TypeSpec.Builder type, PreferenceInformation info, Cached cachedAnnotation,
                                boolean runtimeDefault, String defaultValue, MethodSpec.Builder getterBuilder,
                                MethodInformation methodInformation) {

        if (cachedAnnotation != null) {
            getterBuilder.addStatement("$T __result = ($T) cache.get($S)", info.preferenceType.getObjectType(), info.preferenceType.getObjectType(), info.preferenceName);
            getterBuilder.beginControlFlow("if (__result == null)");
        } else {
            getterBuilder.addStatement("$T __result", info.preferenceType.getType());
        }

        if (runtimeDefault) {
            getterBuilder.beginControlFlow("if (preferences.contains($S))", info.preferenceName);
        }

        String statementPattern = "preferences.get%s(\"%s\", %s)";
        String methodSuffix = Utils.getMethodSuffix(info.preferenceType.getPreferenceType());
        if (info.preferenceType.getPreferenceType() == PreferenceType.OBJECT) {
            getterBuilder.addStatement("$T __serializer = $T.getSerializer()", Serializer.class, Esperandro.class);
            String statement = String.format(statementPattern, methodSuffix, info.preferenceName, defaultValue);
            getterBuilder.addStatement("$T __prefValue = $L", TypeName.get(String.class), statement);
            getterBuilder.addStatement("$L __value = null", info.preferenceType.getTypeName());
            if (methodInformation.defaultAnnotation != null && !getOfClassDefault(methodInformation.defaultAnnotation).equals(getDefaultType())) {
                getterBuilder.beginControlFlow("if (__prefValue == null)");
                getterBuilder.addStatement("__value = new $L()", getOfClassDefault(methodInformation.defaultAnnotation).toString());
                getterBuilder.nextControlFlow("else");
            }
            if (info.preferenceType.isGeneric()) {
                String genericClassName = Utils.createClassNameForPreference(info.preferenceName);
                getterBuilder.addStatement("$L __container = __serializer.deserialize(__prefValue, $L.class)", genericClassName, genericClassName);
                getterBuilder.beginControlFlow("if (__container != null)");
                getterBuilder.addStatement("__value = __container.value");
                getterBuilder.endControlFlow();
                statementPattern = "__value";
            } else {
                getterBuilder.addStatement("__value = __serializer.deserialize(__prefValue, $L.class)", info.preferenceType.getTypeName());
                statementPattern = "__value";
            }
            if (methodInformation.defaultAnnotation != null && !getOfClassDefault(methodInformation.defaultAnnotation).equals(getDefaultType())) {
                getterBuilder.endControlFlow();
            }
        }

        if (info.runtimeDefaultGetter != null && info.runtimeDefaultGetter.defaultAnnotation != null && runtimeDefault) {
            Element element = info.runtimeDefaultGetter.element;
            warner.emitWarning("Pointless @Default Annotation", element);
        }
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

    private TypeMirror getOfClassDefault(Default defaultAnnotation) {
        try {
            defaultAnnotation.ofClass(); // this should throw
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return getDefaultType();
    }

    private TypeMirror getDefaultType() {
        return processingEnv.getElementUtils().getTypeElement(de.devland.esperandro.internal.Default.class.getCanonicalName()).asType();
    }
}
