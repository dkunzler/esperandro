package de.devland.esperandro.processor;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class PreferenceInformation {
    public String preferenceName;
    public PreferenceTypeInformation preferenceType;

    public MethodInformation getter;
    public MethodInformation runtimeDefaultGetter;
    public MethodInformation setter;
    public MethodInformation commitSetter;
    public MethodInformation adder;
    public MethodInformation remover;

}
