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

package de.devland.esperandro.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.devland.esperandro.CacheActions;
import de.devland.esperandro.SharedPreferenceActions;

public class Constants {
    public static final String SHARED_PREFERENCES_ANNOTATION_NAME = "de.devland.esperandro.annotations.SharedPreferences";
    public static final Set<String> SUPER_INTERFACE_BLACKLIST;

    public static final String DECLARED_TYPENAME_STRING = "java.lang.String";
    public static final String DECLARED_TYPENAME_STRINGSET = "java.util.Set<java.lang.String>";

    public static final String SUFFIX_SEPARATOR = "$";

    public static final String PREFIX_GET = "get";
    public static final String PREFIX_SET = "set";


    static {
        Set<String> blacklist = new HashSet<>();
        blacklist.add(SharedPreferenceActions.class.getName());
        blacklist.add(CacheActions.class.getName());
        SUPER_INTERFACE_BLACKLIST = Collections.unmodifiableSet(blacklist);
    }
}
