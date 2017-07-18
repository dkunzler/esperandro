package de.devland.esperandro.processor.generation;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import de.devland.esperandro.processor.Constants;
import de.devland.esperandro.processor.PreferenceInformation;

import javax.lang.model.element.Modifier;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class AdderGenerator {

    public static void generate(TypeSpec.Builder type, PreferenceInformation info) {
        MethodSpec.Builder adder = MethodSpec.methodBuilder(info.preferenceName + Constants.SUFFIX_ADD)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(info.adder.parameterType.getType(), "value")
                .addStatement("$T __pref = this.$L()", info.preferenceType.getObjectType(), info.preferenceName)
                .addStatement("__pref.add(value)")
                .addStatement("this.$L(__pref)", info.preferenceName);
        type.addMethod(adder.build());
    }
}
