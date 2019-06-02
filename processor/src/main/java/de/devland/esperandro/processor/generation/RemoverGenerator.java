package de.devland.esperandro.processor.generation;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.processor.Constants;
import de.devland.esperandro.processor.PreferenceInformation;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class RemoverGenerator {

    public static void generate(TypeSpec.Builder type, PreferenceInformation info) {
        MethodSpec.Builder remover = MethodSpec.methodBuilder(info.preferenceName + Constants.SUFFIX_REMOVE)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(info.remover.parameterType.getType(), "value")
                .addStatement("$T __pref = this.$L()", info.preferenceType.getObjectType(), info.preferenceName)
                .addStatement("__pref.remove(value)")
                .addStatement("this.$L(__pref)", info.preferenceName);
        type.addMethod(remover.build());
    }
}
