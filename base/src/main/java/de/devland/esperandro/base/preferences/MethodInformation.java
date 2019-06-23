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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class MethodInformation {
    public static MethodInformation from(ExecutableElement method) {
        TypeInformation returnType = TypeInformation.from(method.getReturnType());
        TypeInformation parameterType = null;
        if (method.getParameters().size() == 1) {
            parameterType = TypeInformation.from(method.getParameters().get(0).asType());
        }

        return new MethodInformation(method, null, returnType, parameterType);
    }

    public static MethodInformation from(Method method) {
        TypeInformation returnType = TypeInformation.from(method.getGenericReturnType());
        TypeInformation parameterType = null;
        if (method.getGenericParameterTypes().length == 1) {
            parameterType = TypeInformation.from(method.getGenericParameterTypes()[0]);
        }

        return new MethodInformation(null, method, returnType, parameterType);
    }

    private MethodInformation(Element element, Method method,
                              TypeInformation returnType, TypeInformation parameterType) {
        this.element = element;
        this.method = method;
        this.returnType = returnType;
        this.parameterType = parameterType;
    }

    public final Element element;
    public final Method method;
    public final TypeInformation returnType;
    public final TypeInformation parameterType;


    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (method != null) {
            return method.getAnnotation(annotationClass);
        } else if (element != null) {
            return element.getAnnotation(annotationClass);
        }

        return null;
    }

}
