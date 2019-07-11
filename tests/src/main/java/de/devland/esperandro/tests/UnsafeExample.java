package de.devland.esperandro.tests;

import java.util.Set;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;

@SharedPreferences(name = "unsafe")
@Cached(cacheOnPut = true)
public interface UnsafeExample extends SharedPreferenceActions {
    void simpleInt(int simpleInt);
    int simpleInt();

    void container(Container container);
    Container container();

    void string(String string);
    String string();

    void putterWithoutGetter(long value);

    Set<String> getterWithoutPutter();
}
