package de.devland.esperandro.tests;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.UnsafeActions;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.annotations.experimental.Cached;
import de.devland.esperandro.tests.model.Container;

import java.util.Set;

@SharedPreferences(name = "unsafe")
@Cached(cacheOnPut = true)
public interface UnsafeExample extends UnsafeActions, SharedPreferenceActions {
    void simpleInt(int simpleInt);
    int simpleInt();

    void container(Container container);
    Container container();

    void string(String string);
    String string();

    void putterWithoutGetter(long value);

    Set<String> getterWithoutPutter();
}
