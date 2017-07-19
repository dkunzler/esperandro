package de.devland.esperandro.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;

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

    public static String packageNameFromInterface(Element interfaze) {
        QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
        String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
        String packageName = "";
        for (int i = 0; i < split.length - 1; i++) {
            packageName += split[i];
            if (i < split.length - 2) {
                packageName += ".";
            }
        }
        return packageName;
    }

    public static String classNameFromInterface(Element interfaze) {
        QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
        String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
        String typeName = split[split.length - 1];
        return typeName;
    }
}
