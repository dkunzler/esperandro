package de.devland.esperandro.processor;

public class Utils {

    public static String createClassNameForPreference(String valueName) {
        return valueName.substring(0, 1).toUpperCase() + valueName.substring(1);
    }

    public static boolean isGeneric(String typeName) {
        return typeName.matches(".*<.*>");
    }

    public static String getMethodSuffix(PreferenceType preferenceType) {
        String methodSuffix = "";
        switch (preferenceType) {
            case INT:
                methodSuffix = "Int";
                break;
            case LONG:
                methodSuffix = "Long";
                break;
            case FLOAT:
                methodSuffix = "Float";
                break;
            case BOOLEAN:
                methodSuffix = "Boolean";
                //noinspection PointlessBooleanExpression
                break;
            case STRING:
                methodSuffix = "String";
                break;
            case STRINGSET:
                methodSuffix = "StringSet";
                break;
            case OBJECT:
                methodSuffix = "String";
                break;
            case UNKNOWN:
                break;
        }

        return methodSuffix;
    }
}
