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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.SharedPreferences;

public class PreferenceInterface {

    private SharedPreferences preferenceAnnotation;
    private Cached cacheAnnotation;
    private Map<String, List<MethodInformation>> methodsPerPreference = new HashMap<>();
    private Map<String, TypeInformation> typeInformationMap = new HashMap<>();

    public PreferenceInterface(SharedPreferences preferenceAnnotation, Cached cacheAnnotation) {
        this.preferenceAnnotation = preferenceAnnotation;
        this.cacheAnnotation = cacheAnnotation;
    }

    public SharedPreferences getPreferenceAnnotation() {
        return preferenceAnnotation;
    }

    public Cached getCacheAnnotation() {
        return cacheAnnotation;
    }

    public Set<String> getAllPreferences() {
        return typeInformationMap.keySet();
    }

    public List<MethodInformation> getMethodsForPreference(String preferenceName) {
        return methodsPerPreference.get(preferenceName);
    }

    public TypeInformation getTypeOfPreference(String preferenceName) {
        return typeInformationMap.get(preferenceName);
    }

    public void addMethod(String preferenceName, MethodInformation methodInformation) {
        methodInformation.associatedPreference = preferenceName;
        List<MethodInformation> methods = methodsPerPreference.computeIfAbsent(preferenceName, k -> new ArrayList<>());
        methods.add(methodInformation);
    }

    public void addTypeInformation(String preferenceName, TypeInformation typeInformation) {
        if (typeInformation != null) {
            typeInformationMap.putIfAbsent(preferenceName, typeInformation);
        }
    }
}
