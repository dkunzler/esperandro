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
 *
 */
package de.devland.esperandro.processor;

import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class Putter {

    private Map<String, Element> preferenceKeys;

    private Set<TypeKind> validPutterReturnTypes = new HashSet<TypeKind>(
            Arrays.asList(TypeKind.VOID, TypeKind.BOOLEAN));

    public Putter() {
        preferenceKeys = new HashMap<String, Element>();
    }

    public boolean isPutter(ExecutableElement method) {
        boolean isPutter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();
        if (validPutterReturnTypes.contains(returnTypeKind)) {
            if (parameters != null && parameters.size() == 1 && PreferenceType
                    .toPreferenceType(parameters.get(0).asType()) != PreferenceType.NONE) {
                isPutter = true;
            }
        } else {
            // TODO: emit warning here
        }
        return isPutter;
    }

    // TODO: find out when this is used and then add the possibility to detect return type boolean
    public boolean isPutter(Method method) {
        boolean isPutter = false;
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes != null && parameterTypes.length == 1 && method.getReturnType().toString().equals("void")) {
            if (PreferenceType.toPreferenceType(parameterTypes[0]) != PreferenceType.NONE) {
                isPutter = true;
            }
        }

        return isPutter;
    }

    public void createPutterFromModel(ExecutableElement method, JavaWriter writer) throws IOException {
        String valueName = method.getSimpleName().toString();
        preferenceKeys.put(valueName, method);
        TypeMirror parameterType = method.getParameters().get(0).asType();
        PreferenceType preferenceType = PreferenceType.toPreferenceType(parameterType);
        TypeMirror returnType = method.getReturnType();

        createPutter(writer, valueName, valueName, preferenceType, returnType.toString());
    }

    //TODO: find out when this is called and check if the returnType works for void and boolean
    public void createPutterFromReflection(Method method, Element topLevelInterface,
                                           JavaWriter writer) throws IOException {
        String valueName = method.getName();
        preferenceKeys.put(valueName, topLevelInterface);
        Type parameterType = method.getGenericParameterTypes()[0];
        PreferenceType preferenceType = PreferenceType.toPreferenceType(parameterType);
        Class<?> returnType = method.getReturnType();

        createPutter(writer, valueName, valueName, preferenceType, returnType.getSimpleName());
    }

    private void createPutter(JavaWriter writer, String valueName, String value,
                              PreferenceType preferenceType, String returnType) throws IOException {
        writer.emitAnnotation(Override.class);

        boolean shouldReturnValue = returnType.equalsIgnoreCase(Boolean.class.getSimpleName());
        String editorCommitStyle = "apply()";
        StringBuilder statementPattern = new StringBuilder("preferences.edit().put%s(\"%s\", %s).%s");

        writer.beginMethod(returnType, valueName, EsperandroAnnotationProcessor.modPublic, preferenceType.getTypeName(),
                valueName);

        if (shouldReturnValue) {
            statementPattern.insert(0, "return ");
            editorCommitStyle = "commit()";
        }

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
            case OBJECT:
                methodSuffix = "String";
                value = String.format("Esperandro.getSerializer().serialize(%s)", valueName);
                break;
        }

        String statement = String.format(statementPattern.toString(), methodSuffix, valueName, value, editorCommitStyle);
        writer.emitStatement(statement);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    public Map<String, Element> getPreferenceKeys() {
        return preferenceKeys;
    }


}
