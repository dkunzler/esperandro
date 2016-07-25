package de.devland.esperandro;

/**
 * Created by David Kunzler on 25.07.2016.
 */
public interface CacheActions {
    /**
     * If caching is enabled this will reset the underlying cache.
     * This is useful for example when Preferences are changed outside of esperandro.
     */
    void resetCache();
}
