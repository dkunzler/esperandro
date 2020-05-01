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

import javax.lang.model.type.TypeMirror;

import de.devland.esperandro.base.Constants;

public class TypeInformation {
    private EsperandroType esperandroType = EsperandroType.UNKNOWN;
    private boolean isGeneric = false;
    private boolean isVoid = false;
    private boolean isPrimitive = true;
    private String declaredTypeName;
    private TypeMirror typeMirror;

    public static TypeInformation from(TypeMirror typeMirror) {
        TypeInformation result = new TypeInformation();
        result.typeMirror = typeMirror;

        switch (typeMirror.getKind()) {
            case VOID:
                result = getVoid();
                break;
            case BOOLEAN:
                result.esperandroType = EsperandroType.BOOLEAN;
                result.declaredTypeName = "boolean";
                break;
            case INT:
                result.esperandroType = EsperandroType.INT;
                result.declaredTypeName = "int";
                break;
            case LONG:
                result.esperandroType = EsperandroType.LONG;
                result.declaredTypeName = "long";
                break;
            case FLOAT:
                result.esperandroType = EsperandroType.FLOAT;
                result.declaredTypeName = "float";
                break;
            case CHAR:
                result.esperandroType = EsperandroType.CHAR;
                result.declaredTypeName = "char";
                break;
            case BYTE:
                result.esperandroType = EsperandroType.BYTE;
                result.declaredTypeName = "byte";
                break;
            case ARRAY:
            case DECLARED:
                result.isPrimitive = false;
                if (Constants.DECLARED_TYPENAME_STRING.equals(typeMirror.toString())) {
                    result.esperandroType = EsperandroType.STRING;
                    result.declaredTypeName = Constants.DECLARED_TYPENAME_STRING;
                } else if (Constants.DECLARED_TYPENAME_STRINGSET.equals(typeMirror.toString())) {
                    result.esperandroType = EsperandroType.STRINGSET;
                    result.declaredTypeName = Constants.DECLARED_TYPENAME_STRINGSET;
                } else {
                    result.esperandroType = EsperandroType.OBJECT;
                    result.declaredTypeName = typeMirror.toString();
                    result.isGeneric = isGeneric(result.declaredTypeName);
                }
        }

        return result;
    }

    public static TypeInformation getVoid() {
        TypeInformation result = new TypeInformation();
        result.isVoid = true;
        result.esperandroType = EsperandroType.VOID;
        result.declaredTypeName = "void";

        return result;
    }

    private static boolean isGeneric(String typeName) {
        return typeName.matches(".*<.*>");
    }

    public EsperandroType getEsperandroType() {
        return esperandroType;
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
        return isVoid ? TypeName.get(void.class) : TypeName.get(typeMirror);
    }

    public TypeName getObjectType() {
        if (isPrimitive) {
            TypeName result = null;
            switch (esperandroType) {
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
