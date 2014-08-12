package de.devland.esperandro.processor;

public class Utils {

    public static String createClassNameForPreference(String valueName) {
        return valueName.substring(0, 1).toUpperCase() + valueName.substring(1);
    }

}
