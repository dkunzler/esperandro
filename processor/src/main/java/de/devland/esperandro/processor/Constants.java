package de.devland.esperandro.processor;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static final String IMPLEMENTATION_SUFFIX = "$$Impl";
    public static final String SUFFIX_SEPARATOR = "$";
    public static final String SUFFIX_DEFAULT = SUFFIX_SEPARATOR + "Default";
    public static final String SUFFIX_ADD = SUFFIX_SEPARATOR + "Add";
    public static final String SUFFIX_REMOVE = SUFFIX_SEPARATOR + "Remove";

    public static final String[] STANDARD_IMPORTS = new String[]{"android.os.Build", "android.content.Context",
            "android.content.SharedPreferences", "android.annotation.SuppressLint"};
    public static final String SHARED_PREFERENCES_ANNOTATION_NAME = "de.devland.esperandro.annotations.SharedPreferences";

    public static final Set<Modifier> MODIFIER_PRIVATE = new HashSet<Modifier>(Arrays.asList(Modifier.PRIVATE));
    public static final Set<Modifier> MODIFIER_PRIVATE_FINAL = new HashSet<Modifier>(Arrays.asList(Modifier.PRIVATE, Modifier.FINAL));
    public static final Set<Modifier> MODIFIER_PUBLIC = new HashSet<Modifier>(Arrays.asList(Modifier.PUBLIC));


    public static final String DECLARED_TYPENAME_STRING = "java.lang.String";
    public static final String DECLARED_TYPENAME_STRINGSET = "java.util.Set<java.lang.String>";
}
