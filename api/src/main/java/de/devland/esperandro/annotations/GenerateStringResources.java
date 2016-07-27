package de.devland.esperandro.annotations;

/**
 * Created by deekay on 26.07.2016.
 */
public @interface GenerateStringResources {
    String stringPrefix() default "";
    String filePrefix() default "Esperandro";
}
