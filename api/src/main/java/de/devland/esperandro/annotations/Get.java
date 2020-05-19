package de.devland.esperandro.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a method with this annotation to tell the framework that the annotated method should be a getter for the
 * supplied preference.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {
    /**
     * @return The name of the preference to get
     */
    String value();
}
