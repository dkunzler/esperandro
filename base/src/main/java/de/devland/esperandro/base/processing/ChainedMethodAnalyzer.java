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

package de.devland.esperandro.base.processing;

import java.lang.reflect.Method;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import de.devland.esperandro.base.MethodAnalyzer;
import de.devland.esperandro.base.preferences.TypeInformation;

class ChainedMethodAnalyzer implements MethodAnalyzer {

    private final List<MethodAnalyzer> delegates;

    ChainedMethodAnalyzer(List<MethodAnalyzer> delegates) {
        this.delegates = delegates;
    }


    @Override
    public boolean isApplicableMethod(ExecutableElement method) {
        for (MethodAnalyzer analyzer : delegates) {
            if (analyzer.isApplicableMethod(method)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPreferenceName(ExecutableElement method) {
        for (MethodAnalyzer analyzer : delegates) {
            if (analyzer.isApplicableMethod(method)) {
                return analyzer.getPreferenceName(method);
            }
        }
        return null;
    }

    @Override
    public TypeInformation getPreferenceType(ExecutableElement method) {
        for (MethodAnalyzer analyzer : delegates) {
            if (analyzer.isApplicableMethod(method)) {
                return analyzer.getPreferenceType(method);
            }
        }
        return null;
    }

    @Override
    public boolean isApplicableMethod(Method method) {
        for (MethodAnalyzer analyzer : delegates) {
            if (analyzer.isApplicableMethod(method)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPreferenceName(Method method) {
        for (MethodAnalyzer analyzer : delegates) {
            if (analyzer.isApplicableMethod(method)) {
                return analyzer.getPreferenceName(method);
            }
        }
        return null;
    }

    @Override
    public TypeInformation getPreferenceType(Method method) {
        for (MethodAnalyzer analyzer : delegates) {
            if (analyzer.isApplicableMethod(method)) {
                return analyzer.getPreferenceType(method);
            }
        }
        return null;
    }
}
