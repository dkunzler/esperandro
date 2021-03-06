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
import de.devland.esperandro.annotations.Get;
import de.devland.esperandro.base.Utils;
import de.devland.esperandro.base.preferences.EsperandroType;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.MethodOperation;
import de.devland.esperandro.base.preferences.TypeInformation;
import de.devland.esperandro.generation.GetterGenerator;
import de.devland.esperandro.generation.MethodGenerator;
import java8.util.stream.StreamSupport;

public class GetterAnalyzer implements GeneratorAwareAnalyzer {

    public static boolean hasMatch(List<MethodInformation> methods) {
        return StreamSupport.stream(methods)
                .anyMatch(GetterAnalyzer::isApplicableMethodInternal);
    }

    private static boolean isApplicableMethodInternal(MethodInformation method) {
        String methodName = method.getMethodName();

        boolean hasGetAnnotation = method.getAnnotation(Get.class) != null;
        boolean hasSuffixSeparator = methodName.contains(Constants.SUFFIX_SEPARATOR);
        boolean hasGetPrefix = methodName.startsWith(Constants.PREFIX_GET);
        boolean hasParameter = method.parameterType != null;
        boolean hasValidReturnType = method.returnType.getEsperandroType() != EsperandroType.UNKNOWN;

        boolean validAnnotatedMethod = hasGetAnnotation && !hasParameter && hasValidReturnType;
        boolean validUnAnnotatedMethod = !hasSuffixSeparator && hasGetPrefix && !hasParameter && hasValidReturnType;

        return validAnnotatedMethod || validUnAnnotatedMethod;
    }

    private static String getPreferenceNameInternal(MethodInformation method) {
        Get get = method.getAnnotation(Get.class);
        if (get != null) {
            return get.value();
        } else {
            String stripped = method.getMethodName().substring(Constants.PREFIX_GET.length());
            return Utils.lowerCaseFirstLetter(stripped);
        }
    }

    private static TypeInformation getPreferenceTypeInternal(MethodInformation method) {
        return method.returnType;
    }

    @Override
    public MethodGenerator getGenerator() {
        return new GetterGenerator(false);
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
        return MethodOperation.GET;
    }
}
