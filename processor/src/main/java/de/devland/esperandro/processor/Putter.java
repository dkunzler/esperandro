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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Putter {

    private Map<String, ExecutableElement> preferenceKeys;

    public Putter() {
        preferenceKeys = new HashMap<String, ExecutableElement>();
    }

    public boolean isPutter(ExecutableElement method) {
        boolean isPutter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();
        if (parameters != null && parameters.size() == 1 && returnTypeKind.equals(TypeKind.VOID) && PreferenceType
                .toPreferenceType(parameters.get(0).asType()) != PreferenceType.NONE) {
            isPutter = true;
        }
        return isPutter;
    }

    public void createPutter(ExecutableElement method, JavaWriter writer) throws IOException {
        String valueName = method.getSimpleName().toString();
        preferenceKeys.put(valueName, method);

        writer.emitAnnotation(Override.class);
        TypeMirror parameterType = method.getParameters().get(0).asType();
        PreferenceType preferenceType = PreferenceType.toPreferenceType(parameterType);
        writer.beginMethod("void", method.getSimpleName().toString(), Modifier.PUBLIC, preferenceType.getTypeName(),
                method.getSimpleName().toString());
        String statementPattern = "preferences.edit().put%s(\"%s\", %s).commit()";
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
                break;
            case STRING:
                methodSuffix = "String";
                break;
            case STRINGSET:
                methodSuffix = "StringSet";
                break;
        }

        String statement = String.format(statementPattern, methodSuffix, valueName, valueName);
        writer.emitStatement(statement);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    public Map<String, ExecutableElement> getPreferenceKeys() {
        return preferenceKeys;
    }
}
