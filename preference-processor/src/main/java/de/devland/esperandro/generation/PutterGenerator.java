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
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.Constants;
import de.devland.esperandro.Utils;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.base.preferences.EsperandroType;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.TypeInformation;

public class PutterGenerator implements MethodGenerator {
    @Override
    public void generateMethod(TypeSpec.Builder type, MethodInformation methodInformation, Cached cacheAnnotation) {
        boolean returnsBoolean = methodInformation.returnType.getEsperandroType() == EsperandroType.BOOLEAN;
        createInternal(type, methodInformation.parameterType, cacheAnnotation, returnsBoolean, methodInformation);
    }

    private void createInternal(TypeSpec.Builder type, TypeInformation typeInfo, Cached cachedAnnotation,
                                boolean isCommitSetter, MethodInformation methodInformation) {
        String methodName = Constants.PREFIX_SET + Utils.upperCaseFirstLetter(methodInformation.associatedPreference);
        MethodSpec.Builder putterBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(methodInformation.isInternal() ? Modifier.PRIVATE : Modifier.PUBLIC)
                .addParameter(typeInfo.getType(), methodInformation.associatedPreference);
        if (!methodInformation.isInternal()) {
            putterBuilder.addAnnotation(Override.class);
        }
        PreferenceEditorCommitStyle commitStyle = PreferenceEditorCommitStyle.APPLY;
        StringBuilder statementPattern = new StringBuilder("preferences.edit().put%s(\"%s\", %s)");

        if (isCommitSetter) {
            putterBuilder.returns(boolean.class);
            statementPattern.insert(0, "return ");
            commitStyle = PreferenceEditorCommitStyle.COMMIT;
        } else {
            putterBuilder.returns(void.class);
        }

        String methodSuffix = Utils.getMethodSuffix(typeInfo.getEsperandroType());
        String value = methodInformation.associatedPreference;
        switch (typeInfo.getEsperandroType()) {
            case OBJECT:
                if (typeInfo.isGeneric()) {
                    String genericClassName = Utils.createClassNameForPreference(methodInformation.associatedPreference);
                    putterBuilder.addStatement("$L __container = new $L()", genericClassName, genericClassName);
                    putterBuilder.addStatement("__container.value = $L", methodInformation.associatedPreference);
                    value = "Esperandro.getSerializer().serialize(__container)";
                } else {
                    value = String.format("Esperandro.getSerializer().serialize(%s)", methodInformation.associatedPreference);
                }
                break;
            case UNKNOWN:
                break;
        }

        if (cachedAnnotation != null) {
            if (cachedAnnotation.cacheOnPut()) {
                if (typeInfo.isPrimitive()) {
                    putterBuilder.addStatement("cache.put($S, $L)", methodInformation.associatedPreference, methodInformation.associatedPreference);
                } else {
                    putterBuilder.beginControlFlow("if ($L != null)", methodInformation.associatedPreference)
                            .addStatement("cache.put($S, $L)", methodInformation.associatedPreference, methodInformation.associatedPreference)
                            .nextControlFlow("else")
                            .addStatement("cache.remove($S)", methodInformation.associatedPreference)
                            .endControlFlow();
                }
            } else {
                putterBuilder.addStatement("cache.remove($S)", methodInformation.associatedPreference);
            }
        }

        putterBuilder.addStatement(String.format(statementPattern.toString(),
                methodSuffix, methodInformation.associatedPreference, value) + ".$L", commitStyle.getStatementPart());

        type.addMethod(putterBuilder.build());
    }
}
