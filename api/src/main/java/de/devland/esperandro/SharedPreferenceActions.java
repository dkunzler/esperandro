package de.devland.esperandro;

import android.content.SharedPreferences;
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
 *
 */

/**
 * This interface defines some actions to make the use of Esperandro-generated classes easier without using the
 * underlying SharedPreference-instance directly.
 */
public interface SharedPreferenceActions {
    /**
     * @return the underlying SharedPreference instance.
     */
    SharedPreferences get();

    /**
     * Checks if a value for the given key exists.
     *
     * @param key
     *
     * @return true if the given key exists, false otherwise
     */
    boolean contains(String key);

    /**
     * Registers a callback to be invoked when a change happens to a preference.
     *
     * @param listener
     *         The callback that will run.
     */
    void registerOnChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);

    /**
     * Unregisters a previous callback.
     *
     * @param listener
     *         The callback that should be unregistered.
     */
    void unregisterOnChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener);

    /**
     * Clears the complete sharedPreferences of the previously given name. (Be aware that ALL preferences under this
     * name
     * are cleared not only the ones defined in your interface)
     */
    // TODO intelligent clear: only delete those values that are defined in the interface
    void clear();

}
