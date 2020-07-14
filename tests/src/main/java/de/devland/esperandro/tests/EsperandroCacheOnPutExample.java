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
    String getCachedValue();

    void setCachedValue(String cachedValue);

    int getPrimitive();

    void setPrimitive(int primitive);

    boolean setPrimitiveCommit(int primitive);

    void setContainerList(ArrayList<Container> containerList);

    ArrayList<Container> getContainerList();

    ArrayList<Container> getContainerList(ArrayList<Container> runtimeDefault);

    void setContainerListObject(ContainerListObject containerListObject);

    ContainerListObject getContainerListObject();
}
