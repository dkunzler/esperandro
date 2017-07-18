package de.devland.esperandro.processor;

import com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultElement;
import de.devland.esperandro.annotations.Default;

import javax.lang.model.element.Element;
import java.lang.reflect.Method;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class MethodInformation {
    public MethodInformation(Default defaultAnnotation, Element element, Method method,
                             PreferenceTypeInformation returnType, PreferenceTypeInformation parameterType) {
        this.defaultAnnotation = defaultAnnotation;
        this.element = element;
        this.method = method;
        this.returnType = returnType;
        this.parameterType = parameterType;
    }

    public Default defaultAnnotation;
    public Element element;
    public Method method;
    public PreferenceTypeInformation returnType;
    public PreferenceTypeInformation parameterType;
}
