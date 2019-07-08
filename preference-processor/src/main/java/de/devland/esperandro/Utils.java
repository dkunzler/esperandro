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

package de.devland.esperandro;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.QualifiedNameable;

import de.devland.esperandro.base.preferences.EsperandroType;

public class Utils {

    public static String createClassNameForPreference(String valueName) {
        return valueName.substring(0, 1).toUpperCase() + valueName.substring(1);
    }

    public static String classNameFromInterface(Element interfaze) {
        QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
        String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
        return split[split.length - 1]; // last part of qualified name is simple name
    }

    public static String packageNameFromInterface(Element interfaze) {
        QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
        String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
        StringBuilder packageName = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            packageName.append(split[i]);
            if (i < split.length - 2) {
                packageName.append(".");
            }
        }
        return packageName.toString();
    }

    public static boolean isPublic(Element interfaze) {
        QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
        return qualifiedNameable.getModifiers().contains(Modifier.PUBLIC);
    }

    public static String getMethodSuffix(EsperandroType esperandroType) {
        String methodSuffix = "";
        switch (esperandroType) {
            case INT:
            case BYTE:
            case CHAR:
                // cast byte and char into int
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
                break;
            case STRING:
            case OBJECT:
                methodSuffix = "String";
                break;
            case STRINGSET:
                methodSuffix = "StringSet";
                break;
            case UNKNOWN:
                break;
        }

        return methodSuffix;
    }
}
