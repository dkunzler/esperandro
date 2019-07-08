package de.devland.esperandro.tests;

import java.util.List;
import java.util.Set;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;

/**
 * @author David Kunzler on 18.07.2017.
 */
@SharedPreferences
public interface EsperandroCollectionExample extends SharedPreferenceActions {
    boolean listPreference(List<Container> listPreference);
    List<Container> listPreference();
    void listPreference$Add(Container toAdd);
    void listPreference$Remove(Container toRemove);

    boolean setPreference(Set<String> setPreference);
    Set<String> setPreference();
    void setPreference$Add(String toAdd);
    void setPreference$Remove(String toRemove);

    void listDefaultPreference(List<String> listDefault);

    @Default(ofStatement = "new java.util.ArrayList()")
    List<String> listDefaultPreference();

    void stringSetDefaultPreference(Set<String> stringSetDefault);

    @Default(ofStatement = "new java.util.HashSet()")
    Set<String> stringSetDefaultPreference();

}
