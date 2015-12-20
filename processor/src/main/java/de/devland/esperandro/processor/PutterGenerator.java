/*
 * Copyright 2013 David Kunzler Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License
 * at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package de.devland.esperandro.processor;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class PutterGenerator {

    private Map<String, Element> preferenceKeys;

    private Set<TypeKind> validPutterReturnTypes = new HashSet<TypeKind>(Arrays.asList(TypeKind.VOID,
            TypeKind.BOOLEAN));


    public PutterGenerator() {
        preferenceKeys = new HashMap<String, Element>();
    }


    @SuppressWarnings("SimplifiableConditionalExpression")
    public boolean isPutter(ExecutableElement method) {
        boolean isPutter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();

        boolean hasParameter = parameters != null && parameters.size() == 1;
        boolean hasValidReturnType = validPutterReturnTypes.contains(returnTypeKind);
        boolean hasValidPreferenceType = hasParameter ? PreferenceTypeInformation.from(parameters.get(0).asType()).getPreferenceType() != PreferenceType.UNKNOWN : false;
        boolean nameEndsWithDefaultSuffix = method.getSimpleName().toString().endsWith(Constants.RUNTIME_DEFAULT_SUFFIX);

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !nameEndsWithDefaultSuffix) {
            isPutter = true;
        }
        return isPutter;
    }


    public boolean isPutter(Method method) {
        boolean isPutter = false;
        Type[] parameterTypes = method.getGenericParameterTypes();

        boolean hasParameter = parameterTypes != null && parameterTypes.length == 1;
        boolean hasValidReturnType = method.getReturnType().toString().equals("void")
                || method.getReturnType().toString().equals("boolean");
        //noinspection SimplifiableConditionalExpression
        boolean hasValidPreferenceType = hasParameter ? PreferenceTypeInformation.from(parameterTypes[0]).getPreferenceType() != PreferenceType.UNKNOWN : false;
        boolean hasRuntimeDefault = false;

        if (hasParameter) {
            Class<?> parameterType = method.getParameterTypes()[0];

            boolean parameterTypeEqualsReturnType = parameterType.toString().equals(method.getReturnType().toString());
            boolean nameEndsWithDefaultSuffix = method.getName().endsWith(Constants.RUNTIME_DEFAULT_SUFFIX);
            if (parameterTypeEqualsReturnType && nameEndsWithDefaultSuffix) {
                hasRuntimeDefault = true;
            }
        }

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !hasRuntimeDefault) {
            isPutter = true;
        }
        return isPutter;
    }


    public void createPutterFromModel(ExecutableElement method, TypeSpec.Builder type, boolean caching) throws IOException {
        String valueName = method.getSimpleName().toString();
        preferenceKeys.put(valueName, method);
        TypeMirror parameterType = method.getParameters().get(0).asType();
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(parameterType);
        TypeMirror returnType = method.getReturnType();

        createPutter(type, valueName, valueName, preferenceTypeInformation, returnType.toString(), caching);
    }


    public void createPutterFromReflection(Method method, Element topLevelInterface,
                                           TypeSpec.Builder type, boolean caching) throws IOException {
        String valueName = method.getName();
        preferenceKeys.put(valueName, topLevelInterface);
        Type parameterType = method.getGenericParameterTypes()[0];
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(parameterType);
        Class<?> returnType = method.getReturnType();

        createPutter(type, valueName, valueName, preferenceTypeInformation, returnType.toString(), caching);
    }


    private void createPutter(TypeSpec.Builder type, String valueName, String value, PreferenceTypeInformation preferenceTypeInformation,
                              String returnType, boolean caching) throws IOException {
        MethodSpec.Builder putterBuilder = MethodSpec.methodBuilder(valueName)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(preferenceTypeInformation.getType(), valueName);
        boolean shouldReturnValue = returnType.equalsIgnoreCase(Boolean.class.getSimpleName());
        PreferenceEditorCommitStyle commitStyle = PreferenceEditorCommitStyle.APPLY;
        StringBuilder statementPattern = new StringBuilder("preferences.edit().put%s(\"%s\", %s)");

        if (shouldReturnValue) {
            putterBuilder.returns(boolean.class);
            statementPattern.insert(0, "return ");
            commitStyle = PreferenceEditorCommitStyle.COMMIT;
        } else {
            putterBuilder.returns(void.class);
        }

        String methodSuffix = "";
        switch (preferenceTypeInformation.getPreferenceType()) {
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
                if (preferenceTypeInformation.isGeneric()) {
                    String genericClassName = Utils.createClassNameForPreference(valueName);
                    putterBuilder.addStatement("$L __container = new $L()", genericClassName, genericClassName);
                    putterBuilder.addStatement("__container.value = $L", valueName);
                    value = "Esperandro.getSerializer().serialize(__container)";
                } else {
                    value = String.format("Esperandro.getSerializer().serialize(%s)", valueName);
                }
                break;
            case UNKNOWN:
                break;
        }
        // only use apply on API >= 9
        putterBuilder.addStatement(String.format(statementPattern.toString(),
                methodSuffix, valueName, value) + ".$L", commitStyle.getStatementPart());

        type.addMethod(putterBuilder.build());
    }

    public Map<String, Element> getPreferenceKeys() {
        return preferenceKeys;
    }

}
