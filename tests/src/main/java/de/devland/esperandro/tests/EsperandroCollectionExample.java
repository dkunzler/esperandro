package de.devland.esperandro.tests;

import java.util.List;
import java.util.Set;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.Get;
import de.devland.esperandro.annotations.Put;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;

/**
 * @author David Kunzler on 18.07.2017.
 */
@SharedPreferences
public interface EsperandroCollectionExample extends SharedPreferenceActions {
    boolean setListPreference(List<Container> listPreference);
    List<Container> getListPreference();
    boolean listPreference$Add(Container toAdd);
    boolean listPreference$Remove(Container toRemove);

    boolean setSetPreference(Set<String> setPreference);
    Set<String> getSetPreference();
    void setPreference$Add(String toAdd);
    void setPreference$Remove(String toRemove);

    void setListDefaultPreference(List<String> listDefault);

    @Default(ofStatement = "new java.util.ArrayList()")
    List<String> getListDefaultPreference();

    void setStringSetDefaultPreference(Set<String> stringSetDefault);

    @Default(ofStatement = "new java.util.HashSet()")
    Set<String> getStringSetDefaultPreference();


    @Put("realPreferenceName")
    boolean completelyDifferentNamedSetter(Set<String> setPreference);
    @Get("realPreferenceName")
    Set<String> completelyDifferentNamedGetter();
    void realPreferenceName$Add(String toAdd);
    void realPreferenceName$Remove(String toRemove);
}
