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

import de.devland.esperandro.Constants;
import de.devland.esperandro.base.Utils;
import de.devland.esperandro.base.preferences.EsperandroType;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.MethodOperation;
import de.devland.esperandro.base.preferences.TypeInformation;
import de.devland.esperandro.generation.GetterGenerator;
import de.devland.esperandro.generation.MethodGenerator;

public class RuntimeDefaultGetterAnalyzer implements GeneratorAwareAnalyzer {

    private static boolean isApplicableMethodInternal(MethodInformation method) {
        String methodName = method.getMethodName();

        boolean hasSuffixSeparator = methodName.contains(Constants.SUFFIX_SEPARATOR);
        boolean hasGetPrefix = methodName.startsWith(Constants.PREFIX_GET);
        boolean hasSeparator = methodName.contains(Constants.SUFFIX_SEPARATOR);
        boolean hasParameter = method.parameterType != null;
        boolean hasValidReturnType = method.returnType.getEsperandroType() != EsperandroType.UNKNOWN;
        boolean parameterEqualsReturnType = hasParameter && method.returnType.getTypeName().equals(method.parameterType.getTypeName());

        return !hasSuffixSeparator && hasGetPrefix && !hasSeparator && hasParameter && hasValidReturnType && parameterEqualsReturnType;
    }

    private static String getPreferenceNameInternal(MethodInformation method) {
        String stripped = method.getMethodName().substring(Constants.PREFIX_GET.length());
        return Utils.lowerCaseFirstLetter(stripped);
    }

    private static TypeInformation getPreferenceTypeInternal(MethodInformation method) {
        return method.returnType;
    }

    @Override
    public MethodGenerator getGenerator() {
        return new GetterGenerator(true);
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
        return MethodOperation.RUNTIME_GET;
    }
}
