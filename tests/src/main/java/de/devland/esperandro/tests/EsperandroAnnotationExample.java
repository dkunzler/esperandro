package de.devland.esperandro.tests;

import de.devland.esperandro.CacheActions;
import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.Get;
import de.devland.esperandro.annotations.Put;
import de.devland.esperandro.annotations.SharedPreferences;

@Cached
@SharedPreferences(name = "annotations")
public interface EsperandroAnnotationExample extends SharedPreferenceActions, CacheActions {
    @Get("relatedPreference")
    int unrelatedName();

    @Put("relatedPreference")
    void veryUnrelatedName(int unrelated);
}
