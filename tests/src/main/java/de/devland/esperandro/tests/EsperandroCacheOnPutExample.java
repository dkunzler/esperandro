package de.devland.esperandro.tests;

import java.util.ArrayList;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.tests.model.Container;
import de.devland.esperandro.tests.model.ContainerListObject;

/**
 * Created by deekay on 16.12.2015.
 */
@SharedPreferences(name = "cacheOnPutExample")
@Cached(cacheOnPut = true, autoSize = false, cacheSize = 30)
public interface EsperandroCacheOnPutExample extends SharedPreferenceActions {
    String cachedValue();

    void cachedValue(String cachedValue);

    int primitive();

    void primitive(int primitive);

    boolean primitiveCommit(int primitive);

    void containerList(ArrayList<Container> containerList);

    ArrayList<Container> containerList();

    ArrayList<Container> containerList$Default(ArrayList<Container> runtimeDefault);

    void containerListObject(ContainerListObject containerListObject);

    ContainerListObject containerListObject();
}
