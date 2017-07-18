package de.devland.esperandro.processor;

import com.squareup.javapoet.TypeName;
import de.devland.esperandro.annotations.Default;

import javax.lang.model.element.Element;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class PreferenceInformation {
    public String preferenceName;
    public TypeName typeName;
    public PreferenceTypeInformation typeInformation;
    public Default defaultAnnotation;
    public boolean hasGetter;
    public boolean hasCommitSetter;
    public Element getterElement;
    public boolean hasSetter;
    public Element setterElement;
    public boolean hasRuntimeDefaultGetter;
    public Element runtimeDefaultGetterElement;
    public boolean hasAdder;
    public Element adderElement;
    public boolean hasRemover;
    public Element removerElement;

}
