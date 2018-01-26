package de.devland.esperandro.processor;

import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;

public class PreferenceTypeInformation {
    private PreferenceType preferenceType = PreferenceType.UNKNOWN;
    private boolean isGeneric = false;
    private boolean isType = false;
    private boolean isPrimitive = true;
    private String declaredTypeName;
    private Type type;
    private TypeMirror typeMirror;

    public static PreferenceTypeInformation from(TypeMirror typeMirror) {
        PreferenceTypeInformation result = new PreferenceTypeInformation();
        result.typeMirror = typeMirror;

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
            case CHAR:
                result.preferenceType = PreferenceType.CHAR;
                result.declaredTypeName = "char";
                break;
            case BYTE:
                result.preferenceType = PreferenceType.BYTE;
                result.declaredTypeName = "byte";
                break;
            case ARRAY:
            case DECLARED:
                result.isPrimitive = false;
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
        result.isType = true;

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
        } else if (typeString.equals("char")) {
            result.preferenceType = PreferenceType.CHAR;
            result.declaredTypeName = typeString;
        } else if (typeString.equals("byte")) {
            result.preferenceType = PreferenceType.BYTE;
            result.declaredTypeName = typeString;
        } else if (typeString.equals("java.util.Set<java.lang.String>")) {
            result.isPrimitive = false;
            result.preferenceType = PreferenceType.STRINGSET;
            result.declaredTypeName = Constants.DECLARED_TYPENAME_STRINGSET;
        } else if (typeString.equals("class java.lang.String")) {
            result.isPrimitive = false;
            result.preferenceType = PreferenceType.STRING;
            result.declaredTypeName = Constants.DECLARED_TYPENAME_STRING;
        } else if (typeString.startsWith("class ")) {
            result.isPrimitive = false;
            result.preferenceType = PreferenceType.OBJECT;
			// cut off "class " from type name
            result.declaredTypeName = typeString.substring(6);
        } else if (Utils.isGeneric(typeString)) {
            result.isPrimitive = false;
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

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public String getTypeName() {
        return declaredTypeName;
    }

    public TypeName getType() {
        return isType ? TypeName.get(type) : TypeName.get(typeMirror);
    }

    public TypeName getObjectType() {
        if (isPrimitive) {
            TypeName result = null;
            switch (preferenceType) {
                case INT:
                    result = TypeName.get(Integer.class);
                    break;
                case LONG:
                    result = TypeName.get(Long.class);
                    break;
                case FLOAT:
                    result = TypeName.get(Float.class);
                    break;
                case BOOLEAN:
                    result = TypeName.get(Boolean.class);
                    break;
                case BYTE:
                    result = TypeName.get(Byte.class);
                    break;
                case CHAR:
                    result = TypeName.get(Character.class);
                    break;
            }
            return result;
        } else {
            return getType();
        }
    }

}
