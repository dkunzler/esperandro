package de.devland.esperandro.processor.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import de.devland.esperandro.UnsafeActions;
import de.devland.esperandro.annotations.experimental.Cached;
import de.devland.esperandro.processor.PreferenceInformation;

import javax.lang.model.element.Modifier;
import java.util.Collection;

public class UnsafeActionsGenerator {

    public static void createUnsafeActions(TypeSpec.Builder type, Collection<PreferenceInformation> allPreferences, Cached cachedAnnotation) {
        TypeVariableName typeVariable = TypeVariableName.get("V");
        MethodSpec.Builder getValueBuilder = MethodSpec.methodBuilder("getValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addException(UnsafeActions.UnknownKeyException.class)
                .addTypeVariable(typeVariable)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(int.class, "prefId")
                .returns(typeVariable);

        getValueBuilder.addStatement("String prefKey = context.getString(prefId)");

        for (PreferenceInformation info : allPreferences) {
            // when getter is missing, generate one
            if (info.getter == null) {
                new GetterGenerator(null, null).createPrivate(type, info, cachedAnnotation);
            }
            getValueBuilder.beginControlFlow("if (prefKey.equals($S))", info.preferenceName);
            if (info.preferenceType.isPrimitive()) {
                // box
                getValueBuilder.addStatement("return (V) ($T) $L()", info.preferenceType.getObjectType(), info.preferenceName);
            } else {
                getValueBuilder.addStatement("return (V) $L()", info.preferenceName);
            }
            getValueBuilder.endControlFlow();
        }
        getValueBuilder.addStatement("throw new $T(prefKey)", UnsafeActions.UnknownKeyException.class);

        MethodSpec.Builder setValueBuilder = MethodSpec.methodBuilder("setValue")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addException(UnsafeActions.UnknownKeyException.class)
                .addTypeVariable(typeVariable)
                .addParameter(ClassName.get("android.content", "Context"), "context")
                .addParameter(int.class, "prefId")
                .addParameter(typeVariable, "pref");

        setValueBuilder.addStatement("String prefKey = context.getString(prefId)");

        for (PreferenceInformation info : allPreferences) {
            // when setter is missing, generate one
            if (info.setter == null && info.commitSetter == null) {
                new PutterGenerator().createPrivate(type, info, cachedAnnotation);
            }

            setValueBuilder.beginControlFlow("if (prefKey.equals($S))", info.preferenceName);
            if (info.preferenceType.isPrimitive()) {
                // box
                setValueBuilder.addStatement("$L(($T) ($T)pref)", info.preferenceName, info.preferenceType.getType(), info.preferenceType.getObjectType());
            } else {
                setValueBuilder.addStatement("$L(($T)pref)", info.preferenceName, info.preferenceType.getType());
            }
            setValueBuilder.addStatement("return");
            setValueBuilder.endControlFlow();
        }
        setValueBuilder.addStatement("throw new $T(prefKey)", UnsafeActions.UnknownKeyException.class);

        type.addSuperinterface(UnsafeActions.class)
                .addMethod(getValueBuilder.build())
                .addMethod(setValueBuilder.build());
    }
}
