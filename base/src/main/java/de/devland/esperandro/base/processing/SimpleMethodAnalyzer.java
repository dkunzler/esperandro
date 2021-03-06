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

import de.devland.esperandro.base.MethodAnalyzer;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.MethodOperation;
import de.devland.esperandro.base.preferences.TypeInformation;

public class SimpleMethodAnalyzer implements MethodAnalyzer {

    @Override
    public boolean isApplicableMethod(MethodInformation method) {
        return false;
    }

    @Override
    public String getPreferenceName(MethodInformation method) {
        return null;
    }

    @Override
    public TypeInformation getPreferenceType(MethodInformation method) {
        return null;
    }

    @Override
    public MethodOperation getMethodOperation(MethodInformation method) {
        return MethodOperation.UNKNOWN;
    }
}
