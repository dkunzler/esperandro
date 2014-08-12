package de.devland.esperandro.processor;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static final String RUNTIME_DEFAULT_SUFFIX = "$Default";
    public static final String IMPLEMENTATION_SUFFIX = "$$Impl";
    public static final String[] STANDARD_IMPORTS = new String[]{"android.os.Build", "android.content.Context",
            "android.content.SharedPreferences", "android.annotation.SuppressLint"};
    public static final String SHARED_PREFERENCES_ANNOTATION_NAME = "de.devland.esperandro.annotations.SharedPreferences";
    protected static final Set<Modifier> MODIFIER_PRIVATE = new HashSet<Modifier>(Arrays.asList(Modifier.PRIVATE));
    protected static final Set<Modifier> MODIFIER_PUBLIC = new HashSet<Modifier>(Arrays.asList(Modifier.PUBLIC));
}
