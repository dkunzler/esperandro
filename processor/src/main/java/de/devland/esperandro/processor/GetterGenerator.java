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

    private Map<String, Element> preferenceKeys;
    private Map<String, TypeName> genericTypeNames;

    public GetterGenerator(Warner warner) {
        this.warner = warner;
        preferenceKeys = new HashMap<String, Element>();
        genericTypeNames = new HashMap<String, TypeName>();
    }

    public boolean isGetter(ExecutableElement method) {
        boolean isGetter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        PreferenceTypeInformation preferenceTypeInformation = getPreferenceTypeFromMethod(method);

        boolean hasParameters = parameters != null && parameters.size() > 0;
        boolean hasValidReturnType = preferenceTypeInformation.getPreferenceType() != PreferenceType.UNKNOWN;
        boolean hasRuntimeDefault = false;
        boolean nameEndsWithDefaultSuffix = method.getSimpleName().toString().endsWith(Constants.RUNTIME_DEFAULT_SUFFIX);

        if (hasParameters && parameters.size() == 1) { // getter with default can have at most 1 parameter
            VariableElement parameter = parameters.get(0);
            TypeMirror parameterType = parameter.asType();

            boolean parameterTypeEqualsReturnType = parameterType.toString().equals(method.getReturnType().toString());
            if (parameterTypeEqualsReturnType && nameEndsWithDefaultSuffix) {
                hasRuntimeDefault = true;
            }
        }

        if (hasValidReturnType && hasRuntimeDefault) {
            isGetter = true;
        } else if (hasValidReturnType && !nameEndsWithDefaultSuffix && !hasParameters) {
            isGetter = true;
        }

        if (nameEndsWithDefaultSuffix && !hasParameters) {
            String reservedSuffixMessage = String.format("Preferences cannot end with \"%s\" as this " +
                    "suffix is reserved for getters with a runtime default.", Constants.RUNTIME_DEFAULT_SUFFIX);
            warner.emitError(reservedSuffixMessage, method);
        }

        return isGetter;
    }

    public boolean isGetter(Method method) {
        boolean isGetter = false;
        Class<?>[] parameters = method.getParameterTypes();
        Type returnType = method.getGenericReturnType();
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(returnType);

        boolean hasParameters = parameters != null && parameters.length > 0;
        boolean hasValidReturnType = preferenceTypeInformation.getPreferenceType() != PreferenceType.UNKNOWN;
        boolean hasRuntimeDefault = false;

        if (hasParameters && parameters.length == 1) { // getter with default can have at most 1 parameter
            Class<?> parameterType = parameters[0];

            boolean parameterTypeEqualsReturnType = parameterType.toString().equals(method.getReturnType().toString());
            boolean nameEndsWithDefaultSuffix = method.getName().endsWith(Constants.RUNTIME_DEFAULT_SUFFIX);
            if (parameterTypeEqualsReturnType && nameEndsWithDefaultSuffix) {
                hasRuntimeDefault = true;
            }
        }

        if (hasValidReturnType && (!hasParameters || hasRuntimeDefault)) {
            isGetter = true;
        }

        return isGetter;
    }

    public void createGetterFromModel(ExecutableElement method, TypeSpec.Builder type, boolean caching) throws IOException {
        String valueName = method.getSimpleName().toString();
        boolean runtimeDefault = false;

        if (valueName.endsWith(Constants.RUNTIME_DEFAULT_SUFFIX)) {
            runtimeDefault = true;
            valueName = valueName.substring(0, valueName.indexOf(Constants.RUNTIME_DEFAULT_SUFFIX));
        }

        preferenceKeys.put(valueName, method);

        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(method.getReturnType());
        Default defaultAnnotation = method.getAnnotation(Default.class);

        createGetter(defaultAnnotation, method, type, valueName, preferenceTypeInformation, runtimeDefault, caching);
    }

    public void createGetterFromReflection(Method method, Element topLevelInterface,
                                           TypeSpec.Builder type, boolean caching) throws IOException {
        String valueName = method.getName();

        boolean runtimeDefault = false;

        if (valueName.endsWith(Constants.RUNTIME_DEFAULT_SUFFIX)) {
            runtimeDefault = true;
            valueName = valueName.substring(0, valueName.indexOf(Constants.RUNTIME_DEFAULT_SUFFIX));
        }

        preferenceKeys.put(valueName, topLevelInterface);

        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(method.getGenericReturnType());
        Default defaultAnnotation = method.getAnnotation(Default.class);

        createGetter(defaultAnnotation, topLevelInterface, type, valueName, preferenceTypeInformation, runtimeDefault, caching);
    }

    private PreferenceTypeInformation getPreferenceTypeFromMethod(ExecutableElement method) {
        TypeMirror returnType = method.getReturnType();
        return PreferenceTypeInformation.from(returnType);
    }


    private void createGetter(Default defaultAnnotation, Element element, TypeSpec.Builder type, String valueName,
                              PreferenceTypeInformation preferenceTypeInformation, boolean runtimeDefault, boolean caching) throws IOException {
        MethodSpec.Builder getterBuilder = initGetter(valueName, preferenceTypeInformation, runtimeDefault);

        if (caching) {
            getterBuilder.addStatement("$T __result = ($T) cache.get($S)", preferenceTypeInformation.getType(), preferenceTypeInformation.getType(), valueName);
            getterBuilder.beginControlFlow("if (__result == null)");
        } else {
            getterBuilder.addStatement("$T __result", preferenceTypeInformation.getType());
        }

        if (runtimeDefault) {
            getterBuilder.beginControlFlow("if (preferences.contains($S))", valueName);
        }

        String statementPattern = "preferences.get%s(\"%s\", %s)";
        String methodSuffix = getMethodSuffix(preferenceTypeInformation.getPreferenceType());
        String defaultValue = getDefaultValue(defaultAnnotation, preferenceTypeInformation.getPreferenceType(), element);
        if (preferenceTypeInformation.getPreferenceType() == PreferenceType.OBJECT) {
            getterBuilder.addStatement("$T __serializer = $T.getSerializer()", Serializer.class, Esperandro.class);
            if (preferenceTypeInformation.isGeneric()) {
                String genericClassName = Utils.createClassNameForPreference(valueName);
                genericTypeNames.put(genericClassName, preferenceTypeInformation.getType());
                String statement = String.format(statementPattern, methodSuffix, valueName, defaultValue);
                getterBuilder.addStatement("$L __container = __serializer.deserialize($L, $L.class)", genericClassName, statement, genericClassName);
                getterBuilder.addStatement("$L __value = null", preferenceTypeInformation.getTypeName());
                getterBuilder.beginControlFlow("if (__container != null)");
                getterBuilder.addStatement("__value = __container.value");
                getterBuilder.endControlFlow();
                statementPattern = "__value";
            } else {
                statementPattern = String.format("__serializer.deserialize(%s, %s.class)",
                        statementPattern, preferenceTypeInformation.getTypeName());
            }
        }

        if (defaultAnnotation != null && runtimeDefault) {
            warner.emitWarning("Pointless @Default Annotation", element);
        }
        String statement = String.format(statementPattern, methodSuffix, valueName, defaultValue);
        getterBuilder.addStatement("__result = $L", statement);
        if (runtimeDefault) {
            getterBuilder.nextControlFlow("else")
                    .addStatement("__result = defaultValue")
                    .endControlFlow();
        }

        if (caching) {
            getterBuilder.addStatement("cache.put($S, __result);", valueName);
            getterBuilder.endControlFlow();
        }
        getterBuilder.addStatement("return __result");
        type.addMethod(getterBuilder.build());
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

    private String getMethodSuffix(PreferenceType preferenceType) {
        String methodSuffix = "";
        switch (preferenceType) {
            case INT:
                methodSuffix = "Int";
                break;
            case LONG:
                methodSuffix = "Long";
                break;
            case FLOAT:
                methodSuffix = "Float";
                break;
            case BOOLEAN:
                methodSuffix = "Boolean";
                //noinspection PointlessBooleanExpression
                break;
            case STRING:
                methodSuffix = "String";
                break;
            case STRINGSET:
                methodSuffix = "StringSet";
                break;
            case OBJECT:
                methodSuffix = "String";
                break;
            case UNKNOWN:
                break;
        }

        return methodSuffix;
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

    public Map<String, Element> getPreferenceKeys() {
        return preferenceKeys;
    }

    public Map<String, TypeName> getGenericTypeNames() {
        return genericTypeNames;
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


}
