package de.devland.esperandro.annotations;

import android.annotation.TargetApi;
import android.os.Build;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enabled caching on SharedPreference
 */
@Target(TYPE)
@Retention(RUNTIME)
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public @interface Cached {
    int cacheSize() default 20;
    boolean cacheOnPut() default false;
}
