/*
 * Copyright 2013 David Kunzler
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package de.devland.esperandro.base.preferences;

import com.squareup.javapoet.TypeName;

import java.lang.reflect.Type;

import javax.lang.model.type.TypeMirror;

import de.devland.esperandro.base.Constants;

public class TypeInformation {
    private PreferenceType preferenceType = PreferenceType.UNKNOWN;
    private boolean isGeneric = false;
    private boolean isType = false;
    private boolean isPrimitive = true;
    private String declaredTypeName;
    private Type type;
    private TypeMirror typeMirror;

    public static TypeInformation from(TypeMirror typeMirror) {
        TypeInformation result = new TypeInformation();
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
                    result.isGeneric = isGeneric(result.declaredTypeName);
                }
        }

        return result;
    }

    public static TypeInformation from(Type type) {
        TypeInformation result = new TypeInformation();
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
        } else if (isGeneric(typeString)) {
            result.isPrimitive = false;
            result.preferenceType = PreferenceType.OBJECT;
            result.declaredTypeName = typeString;
            result.isGeneric = true;
        }

        return result;
    }

    private static boolean isGeneric(String typeName) {
        return typeName.matches(".*<.*>");
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
