package de.devland.esperandro.tests;

import java.util.Set;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.GenerateStringResources;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;

@SharedPreferences(name = "unsafe")
@Cached(cacheOnPut = true)
@GenerateStringResources(stringPrefix = "foo_")
public interface UnsafeExample extends SharedPreferenceActions {
    void setSimpleInt(int simpleInt);
    int getSimpleInt();

    void setContainer(Container container);
    Container getContainer();

    void setString(String string);
    String getString();

    void setPutterWithoutGetter(long value);

    Set<String> getGetterWithoutPutter();
}
