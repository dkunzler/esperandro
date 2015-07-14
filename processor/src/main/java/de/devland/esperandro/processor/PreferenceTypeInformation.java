package de.devland.esperandro.processor;

import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;

public class PreferenceTypeInformation {
    private PreferenceType preferenceType = PreferenceType.UNKNOWN;
    private boolean isGeneric = false;
    private String declaredTypeName;
    private Type type;

    public static PreferenceTypeInformation from(TypeMirror typeMirror) {
        PreferenceTypeInformation result = new PreferenceTypeInformation();

        switch (typeMirror.getKind()) {
            case BOOLEAN:
                result.preferenceType = PreferenceType.BOOLEAN;
                result.declaredTypeName = "boolean";
                break;
            case INT:
                result.preferenceType = PreferenceType.INT;
                result.declaredTypeName = "int";
                break;
            case LONG:
                result.preferenceType = PreferenceType.LONG;
                result.declaredTypeName = "long";
                break;
            case FLOAT:
                result.preferenceType = PreferenceType.FLOAT;
                result.declaredTypeName = "float";
                break;
            case DECLARED:
                if (Constants.DECLARED_TYPENAME_STRING.equals(typeMirror.toString())) {
                    result.preferenceType = PreferenceType.STRING;
                    result.declaredTypeName = Constants.DECLARED_TYPENAME_STRING;
                } else if (Constants.DECLARED_TYPENAME_STRINGSET.equals(typeMirror.toString())) {
                    result.preferenceType = PreferenceType.STRINGSET;
                    result.declaredTypeName = Constants.DECLARED_TYPENAME_STRINGSET;
                } else {
                    result.preferenceType = PreferenceType.OBJECT;
                    result.declaredTypeName = typeMirror.toString();
                    result.isGeneric = Utils.isGeneric(result.declaredTypeName);
                }
        }

        return result;
    }

    public static PreferenceTypeInformation from(Type type) {
        PreferenceTypeInformation result = new PreferenceTypeInformation();
        result.type = type;

        String typeString = type.toString();

        if (typeString.equals("int")) {
            result.preferenceType = PreferenceType.INT;
            result.declaredTypeName = typeString;
        } else if (typeString.equals("long")) {
            result.preferenceType = PreferenceType.LONG;
            result.declaredTypeName = typeString;
        } else if (typeString.equals("float")) {
            result.preferenceType = PreferenceType.FLOAT;
            result.declaredTypeName = typeString;
        } else if (typeString.equals("boolean")) {
            result.preferenceType = PreferenceType.BOOLEAN;
            result.declaredTypeName = typeString;
        } else if (typeString.equals("java.util.Set<java.lang.String>")) {
            result.preferenceType = PreferenceType.STRINGSET;
            result.declaredTypeName = Constants.DECLARED_TYPENAME_STRINGSET;
        } else if (typeString.equals("class java.lang.String")) {
            result.preferenceType = PreferenceType.STRING;
            result.declaredTypeName = Constants.DECLARED_TYPENAME_STRING;
        } else if (typeString.startsWith("class ")) {
            result.preferenceType = PreferenceType.OBJECT;
			// cut off "class " from type name
            result.declaredTypeName = typeString.substring(6);
        } else if (Utils.isGeneric(typeString)) {
            result.preferenceType = PreferenceType.OBJECT;
            result.declaredTypeName = typeString;
            result.isGeneric = true;
        }

        return result;
    }

    public PreferenceType getPreferenceType() {
        return preferenceType;
    }

    public boolean isGeneric() {
        return isGeneric;
    }

    public String getTypeName() {
        return declaredTypeName;
    }

    public Type getType() {
        return type;
    }
}
