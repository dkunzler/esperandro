package de.devland.esperandro.annotations.experimental;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enabled caching on SharedPreference interface.
 * IMPORTANT: caching does not reflect changes made via Android internal methods of altering SharedPreference files.
 * Only changes made via esperandro generated classes can be reflected in the cache. Extend
 * {@link de.devland.esperandro.CacheActions} in your interface to interact directly with the cache.
 * It is not possible to use this annotation on an interface for the default preferences since it is very likely that
 * these are changed in {@link android.preference.PreferenceActivity} oder {@link android.preference.PreferenceFragment}.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Cached {
    /**
     * defines the size of the underlying cache
     */
    int cacheSize() default 20;

    /**
     * defines if values are updated when a put occurs (otherwise value in cache is deleted on put)
     */
    boolean cacheOnPut() default false;

    /**
     * indicates if LruCache from support library v4 is used or the LruCache integrated in the
     * Android SDK (version 12 and up)
     */
    boolean support() default false;
}
