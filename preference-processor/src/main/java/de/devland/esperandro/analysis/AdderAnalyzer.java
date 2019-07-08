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
import de.devland.esperandro.base.preferences.EsperandroType;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.TypeInformation;
import de.devland.esperandro.generation.CollectionActionGenerator;
import de.devland.esperandro.generation.MethodGenerator;

public class AdderAnalyzer implements GeneratorAwareAnalyzer {

    @Override
    public boolean isApplicableMethod(MethodInformation method) {
        boolean hasAdderSuffix = method.getMethodName().endsWith(Constants.SUFFIX_ADD);
        boolean hasValidParameter = method.parameterType != null && method.parameterType.getEsperandroType() != EsperandroType.UNKNOWN;
        boolean hasValidReturnType = method.returnType.getEsperandroType() == EsperandroType.VOID;

        return hasAdderSuffix && hasValidParameter && hasValidReturnType;
    }

    @Override
    public String getPreferenceName(MethodInformation method) {
        String methodName = method.getMethodName();
        return methodName.substring(0, methodName.length() - Constants.SUFFIX_ADD.length());
    }

    @Override
    public TypeInformation getPreferenceType(MethodInformation method) {
        // type of preference cannot be deducted by the adder
        return null;
    }

    @Override
    public MethodGenerator getGenerator() {
        return new CollectionActionGenerator("add");
    }
}
