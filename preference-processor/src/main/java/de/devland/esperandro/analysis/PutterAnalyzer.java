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

package de.devland.esperandro.analysis;

import java.util.List;

import de.devland.esperandro.Constants;
import de.devland.esperandro.annotations.Put;
import de.devland.esperandro.base.Utils;
import de.devland.esperandro.base.preferences.EsperandroType;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.MethodOperation;
import de.devland.esperandro.base.preferences.TypeInformation;
import de.devland.esperandro.generation.MethodGenerator;
import de.devland.esperandro.generation.PutterGenerator;
import java8.util.stream.StreamSupport;

public class PutterAnalyzer implements GeneratorAwareAnalyzer {

    public static boolean hasMatch(List<MethodInformation> methods) {
        return StreamSupport.stream(methods)
                .anyMatch(PutterAnalyzer::isApplicableMethodInternal);
    }

    private static boolean isApplicableMethodInternal(MethodInformation method) {
        String methodName = method.getMethodName();

        boolean hasPutAnnotation = method.getAnnotation(Put.class) != null;
        boolean hasSuffixSeparator = methodName.contains(Constants.SUFFIX_SEPARATOR);
        boolean hasSetPrefix = methodName.startsWith(Constants.PREFIX_SET);
        boolean hasValidParameter = method.parameterType != null && method.parameterType.getEsperandroType() != EsperandroType.UNKNOWN;
        boolean hasValidReturnType = method.returnType.getEsperandroType() == EsperandroType.VOID
                || method.returnType.getEsperandroType() == EsperandroType.BOOLEAN;

        boolean validAnnotatedMethod = hasPutAnnotation && hasValidParameter && hasValidReturnType;
        boolean validUnAnnotatedMethod = !hasSuffixSeparator && hasSetPrefix && hasValidParameter && hasValidReturnType;

        return validAnnotatedMethod || validUnAnnotatedMethod;
    }

    private static String getPreferenceNameInternal(MethodInformation method) {
        Put put = method.getAnnotation(Put.class);
        if (put != null) {
            return put.value();
        } else {
            String stripped = method.getMethodName().substring(Constants.PREFIX_SET.length());
            return Utils.lowerCaseFirstLetter(stripped);
        }
    }

    private static TypeInformation getPreferenceTypeInternal(MethodInformation method) {
        return method.parameterType;
    }

    @Override
    public MethodGenerator getGenerator() {
        return new PutterGenerator();
    }

    @Override
    public boolean isApplicableMethod(MethodInformation method) {
        return isApplicableMethodInternal(method);
    }

    @Override
    public String getPreferenceName(MethodInformation method) {
        return getPreferenceNameInternal(method);
    }

    @Override
    public TypeInformation getPreferenceType(MethodInformation method) {
        return getPreferenceTypeInternal(method);
    }

    @Override
    public MethodOperation getMethodOperation(MethodInformation method) {
        return MethodOperation.PUT;
    }
}
