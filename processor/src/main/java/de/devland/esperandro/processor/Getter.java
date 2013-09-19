package de.devland.esperandro.processor;/*
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
 *
 */

import com.squareup.javawriter.JavaWriter;
import de.devland.esperandro.annotations.Default;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Getter {

    private Warner warner;

    private Map<String, Element> preferenceKeys;

    public Getter(Warner warner) {
        this.warner = warner;
        preferenceKeys = new HashMap<String, Element>();
    }

    public boolean isGetter(ExecutableElement method) {
        boolean isGetter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        PreferenceType preferenceType = PreferenceType.toPreferenceType(returnType);

        if ((parameters == null || parameters.size() == 0) && preferenceType != PreferenceType.NONE) {
            isGetter = true;
        }
        return isGetter;
    }

    public boolean isGetter(Method method) {
        boolean isGetter = false;
        Class<?>[] parameters = method.getParameterTypes();
        Type returnType = method.getGenericReturnType();
        PreferenceType preferenceType = PreferenceType.toPreferenceType(returnType);

        if ((parameters == null || parameters.length == 0) && preferenceType != PreferenceType.NONE) {
            isGetter = true;
        }

        return isGetter;
    }

    private String getClassDefinitionForType(TypeMirror type) {
        return String.format("java.lang.Class<%s>", type.toString());
    }

    public void createGetterFromModel(ExecutableElement method, JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        String valueName = method.getSimpleName().toString();
        preferenceKeys.put(valueName, method);

        PreferenceType preferenceType = PreferenceType.toPreferenceType(method.getReturnType());
        Default defaultAnnotation = method.getAnnotation(Default.class);

        createGetter(defaultAnnotation, method, writer, valueName, preferenceType);
    }

    public void createGetterFromReflection(Method method, Element topLevelInterface,
                                           JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        String valueName = method.getName();
        preferenceKeys.put(valueName, topLevelInterface);

        PreferenceType preferenceType = PreferenceType.toPreferenceType(method.getGenericReturnType());
        Default defaultAnnotation = method.getAnnotation(Default.class);

        createGetter(defaultAnnotation, topLevelInterface, writer, valueName, preferenceType);
    }


    private void createGetter(Default defaultAnnotation, Element element, JavaWriter writer, String valueName,
                              PreferenceType preferenceType) throws IOException {
        boolean hasDefaultAnnotation = defaultAnnotation != null;

        boolean allDefaults = false;
        if (hasDefaultAnnotation) {
            allDefaults = hasAllDefaults(defaultAnnotation);
        }
        String statementPattern = "preferences.get%s(\"%s\", %s)";
        String methodSuffix = "";
        String defaultValue = "";
        switch (preferenceType) {
            case INT:
                methodSuffix = "Int";
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofInt() == Default.intDefault) {
                    warner.emitMissingDefaultWarning("int", element);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofInt()) : String.valueOf
                        (Default.intDefault);
                break;
            case LONG:
                methodSuffix = "Long";
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofLong() == Default.longDefault) {
                    warner.emitMissingDefaultWarning("long", element);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofLong()) : String.valueOf
                        (Default.longDefault);
                defaultValue += "l";
                break;
            case FLOAT:
                methodSuffix = "Float";
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofFloat() == Default.floatDefault) {
                    warner.emitMissingDefaultWarning("float", element);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofFloat()) : String.valueOf
                        (Default.floatDefault);
                defaultValue += "f";
                break;
            case BOOLEAN:
                methodSuffix = "Boolean";
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
                methodSuffix = "String";
                defaultValue = (hasDefaultAnnotation ? ("\"" + defaultAnnotation.ofString() + "\"") : ("\"" + Default
                        .stringDefault + "\""));
                break;
            case STRINGSET:
                if (hasDefaultAnnotation) {
                    warner.emitWarning("No default for Set<String> preferences allowed.", element);
                }
                methodSuffix = "StringSet";
                defaultValue = "null";
                break;
            case OBJECT:
                if (hasDefaultAnnotation) {
                    warner.emitWarning("No default for Object preferences allowed.", element);
                }
                methodSuffix = "String";
                defaultValue = "null";
                statementPattern = String.format("Esperandro.getSerializer().deserialize(%s, %s.class)",
                        statementPattern, preferenceType.getTypeName());
                break;
        }

        writer.beginMethod(preferenceType.getTypeName(), valueName, EsperandroAnnotationProcessor.modPublic);

        String statement = String.format(statementPattern, methodSuffix, valueName, defaultValue);
        writer.emitStatement("return %s", statement);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    public Map<String, Element> getPreferenceKeys() {
        return preferenceKeys;
    }


    private boolean hasAllDefaults(Default defaultAnnotation) {
        boolean hasAllDefaults = true;

        hasAllDefaults &= defaultAnnotation.ofBoolean() == Default.booleanDefault;
        hasAllDefaults &= defaultAnnotation.ofInt() == Default.intDefault;
        hasAllDefaults &= defaultAnnotation.ofFloat() == Default.floatDefault;
        hasAllDefaults &= defaultAnnotation.ofLong() == Default.longDefault;
        hasAllDefaults &= defaultAnnotation.ofString().equals(Default.stringDefault);

        return hasAllDefaults;
    }


}
