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
package de.devland.esperandro.processor;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;

public enum PreferenceType {
    NONE(null, null), INT(TypeKind.INT), LONG(TypeKind.LONG), FLOAT(TypeKind.FLOAT), BOOLEAN(TypeKind.BOOLEAN),
    STRING(TypeKind.DECLARED, "java.lang.String"), STRINGSET(TypeKind.DECLARED, "java.util.Set<java.lang.String>"),
    OBJECT(TypeKind.DECLARED, null);
    private String declaredTypeName;
    private boolean isGeneric = false;

    PreferenceType(TypeKind typeKind) {
        this.declaredTypeName = null;
    }

    PreferenceType(TypeKind typeKind, String declaredTypeName) {
        this.declaredTypeName = declaredTypeName;
    }

    public static PreferenceType toPreferenceType(TypeMirror typeMirror) {
        PreferenceType type = NONE;

        switch (typeMirror.getKind()) {
            case BOOLEAN:
                type = BOOLEAN;
                break;
            case INT:
                type = INT;
                break;
            case LONG:
                type = LONG;
                break;
            case FLOAT:
                type = FLOAT;
                break;
            case DECLARED:
                if (typeMirror.toString().equals(PreferenceType.STRING.declaredTypeName)) {
                    type = STRING;
                } else if (typeMirror.toString().equals(PreferenceType.STRINGSET.declaredTypeName)) {
                    type = STRINGSET;
                } else {
                    type = OBJECT;
                    type.declaredTypeName = typeMirror.toString();
                    type.isGeneric = isGeneric(type.declaredTypeName);
                }
        }

        return type;
    }

    public static PreferenceType toPreferenceType(Type type) {
        String typeString = type.toString();
        PreferenceType preferenceType = NONE;

        if (typeString.equals("int")) {
            preferenceType = INT;
        } else if (typeString.equals("long")) {
            preferenceType = LONG;
        } else if (typeString.equals("float")) {
            preferenceType = FLOAT;
        } else if (typeString.equals("boolean")) {
            preferenceType = BOOLEAN;
        } else if (typeString.equals("java.util.Set<java.lang.String>")) {
            preferenceType = STRINGSET;
        } else if (typeString.equals("class java.lang.String")) {
            preferenceType = STRING;
        } else if (typeString.startsWith("class ")) {
            preferenceType = OBJECT;
            preferenceType.declaredTypeName = typeString.substring(6);
            preferenceType.isGeneric = false;
        } else if (isGeneric(typeString)) {
            preferenceType.declaredTypeName = typeString;
            preferenceType.isGeneric = true;
        }
        return preferenceType;
    }

    private static boolean isGeneric(String typeName) {
        return typeName.matches(".*<.*>");
    }

    public String getTypeName() {
        String typeName = declaredTypeName;
        switch (this) {
            case INT:
                typeName = "int";
                break;
            case LONG:
                typeName = "long";
                break;
            case FLOAT:
                typeName = "float";
                break;
            case BOOLEAN:
                typeName = "boolean";
                break;
        }

        return typeName;
    }

    public boolean isGeneric() {
        return isGeneric;
    }
}
