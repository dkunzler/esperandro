package de.devland.esperandro.annotations;

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
     * Defines the size of the underlying cache. This value is ignored if autoSize is set to true.
     */
    int cacheSize() default 20;

    /**
     *
     * If set to true the cacheSize will be automatically determined to hold all preferences of this cached Preference
     * Interface. If set to false the value of cacheSize will be used.
     */
    boolean autoSize() default true;

    /**
     * defines if values are updated when a put occurs (otherwise value in cache is deleted on put)
     */
    boolean cacheOnPut() default false;

    /**
     * indicates which LruCache implementation is used:
     * - FRAMEWORK means the integrated version in the Android SDK (version 12 and up)
     * - SUPPORT_V$ means the class from the support-v4 library
     * - ANDROID_X means the version from the androidx collection library
     */
    CacheVersion cacheVersion() default CacheVersion.FRAMEWORK;


    enum CacheVersion {
        FRAMEWORK, SUPPORT_V4, ANDROID_X
    }

}
