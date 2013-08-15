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

import com.squareup.java.JavaWriter;
import de.devland.esperandro.annotations.Default;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Getter {

    private Warner warner;

    private Map<String, ExecutableElement> preferenceKeys;

    public Getter(Warner warner) {
        this.warner = warner;
        preferenceKeys = new HashMap<String, ExecutableElement>();
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

    private String getClassDefinitionForType(TypeMirror type) {
        return String.format("java.lang.Class<%s>", type.toString());
    }

    public void createGetter(ExecutableElement method, JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        String valueName = method.getSimpleName().toString();
        preferenceKeys.put(valueName, method);

        PreferenceType preferenceType = PreferenceType.toPreferenceType(method.getReturnType());

        Default defaultAnnotation = method.getAnnotation(Default.class);
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
                    warner.emitMissingDefaultWarning("int", method);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofInt()) : String.valueOf
                        (Default.intDefault);
                break;
            case LONG:
                methodSuffix = "Long";
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofLong() == Default.longDefault) {
                    warner.emitMissingDefaultWarning("long", method);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofLong()) : String.valueOf
                        (Default.longDefault);
                defaultValue += "l";
                break;
            case FLOAT:
                methodSuffix = "Float";
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofFloat() == Default.floatDefault) {
                    warner.emitMissingDefaultWarning("float", method);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofFloat()) : String.valueOf
                        (Default.floatDefault);
                defaultValue += "f";
                break;
            case BOOLEAN:
                methodSuffix = "Boolean";
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofBoolean() == Default.booleanDefault) {
                    warner.emitMissingDefaultWarning("boolean", method);
                }
                defaultValue = hasDefaultAnnotation ? String.valueOf(defaultAnnotation.ofBoolean()) : String.valueOf
                        (Default.booleanDefault);
                break;
            case STRING:
                if (hasDefaultAnnotation && !allDefaults && defaultAnnotation.ofString().equals(Default
                        .stringDefault)) {
                    warner.emitMissingDefaultWarning("String", method);
                }
                methodSuffix = "String";
                defaultValue = (hasDefaultAnnotation ? ("\"" + defaultAnnotation.ofString() + "\"") : ("\"" + Default
                        .stringDefault + "\""));
                break;
            case STRINGSET:
                if (hasDefaultAnnotation) {
                    warner.emitWarning("No default for Set<String> preferences allowed.", method);
                }
                methodSuffix = "StringSet";
                defaultValue = "null";
                break;
            case OBJECT:
                if (hasDefaultAnnotation) {
                    warner.emitWarning("No default for Object preferences allowed.", method);
                }
                methodSuffix = "String";
                defaultValue = "null";
                statementPattern = String.format("Esperandro.getSerializer().deserialize(%s, %s.class)",
                        statementPattern, preferenceType.getTypeName());
                break;
        }

        writer.beginMethod(preferenceType.getTypeName(), method.getSimpleName().toString(), Modifier.PUBLIC);

        String statement = String.format(statementPattern, methodSuffix, valueName, defaultValue);
        writer.emitStatement("return %s", statement);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    public Map<String, ExecutableElement> getPreferenceKeys() {
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
