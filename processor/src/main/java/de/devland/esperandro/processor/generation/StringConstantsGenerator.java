package de.devland.esperandro.processor.generation;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import de.devland.esperandro.processor.Constants;
import de.devland.esperandro.processor.PreferenceInformation;
import de.devland.esperandro.processor.Utils;

/**
 * Created by deekay on 18.01.2017.
 */
public class StringConstantsGenerator {

    public static void createConstantsClass(Filer filer, Element interfaze, Collection<PreferenceInformation> keys) throws IOException {
        TypeSpec.Builder result;

        String typeName = Utils.classNameFromInterface(interfaze) + Constants.SUFFIX_KEYS;
        result = TypeSpec.classBuilder(typeName);

        if (Utils.isPublic(interfaze)) {
            result.addModifiers(Modifier.PUBLIC);
        }

        for (PreferenceInformation info : keys) {
            result.addField(generateField(info.preferenceName));
        }

        String packageName = Utils.packageNameFromInterface(interfaze);
        JavaFile javaFile = JavaFile.builder(packageName, result.build())
                .build();
        javaFile.writeTo(filer);
    }

    private static FieldSpec generateField(String key) {
        FieldSpec.Builder builder = FieldSpec.builder(String.class, key, Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC);
        builder.initializer("$S", key);
        return builder.build();
    }
}