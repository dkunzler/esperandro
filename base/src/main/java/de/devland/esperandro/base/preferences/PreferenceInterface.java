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

package de.devland.esperandro.base.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.devland.esperandro.annotations.Cached;

public class PreferenceInterface {

    private Cached cacheAnnotation;
    private Map<String, List<MethodInformation>> methodsPerPreference = new HashMap<>();
    private Map<String, TypeInformation> typeInformationMap = new HashMap<>();

    public PreferenceInterface(Cached cacheAnnotation) {
        this.cacheAnnotation = cacheAnnotation;
    }

    public Cached getCacheAnnotation() {
        return cacheAnnotation;
    }

    public Set<String> getAllPreferences() {
        // TODO
        return null;
    }

    public List<MethodInformation> getMethodsForPreference(String preferenceName) {
        // TODO
        return null;
    }

    public TypeInformation getTypeOfPreference(String preferenceName) {
        // TODO
        return null;
    }

    public void addMethod(String preferenceName, MethodInformation methodInformation) {
        // TODO
    }

    public void addTypeInformation(String preferenceName, TypeInformation typeInformation) {
        // TODO
    }
}
