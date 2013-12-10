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

package de.devland.esperandro;

/**
 * Values correspond to the respective android equivalent of Context.MODE_${SharedPreferenceMode}.
 */
public enum SharedPreferenceMode {
    PRIVATE("Context.MODE_PRIVATE"), WORLD_READABLE("Context.MODE_WORLD_READABLE"),
    WORLD_WRITABLE("Context.MODE_WORLD_WRITEABLE"),
    MULTI_PROCESS("Context.MODE_MULTI_PROCESS");
    String androidSharedPreferenceMode;

    private SharedPreferenceMode(String androidMode) {
        this.androidSharedPreferenceMode = androidMode;
    }

    public String getSharedPreferenceModeStatement() {
        return androidSharedPreferenceMode;
    }
}
