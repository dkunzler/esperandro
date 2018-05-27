package de.devland.esperandro.processor.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collection;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.processor.PreferenceInformation;
import de.devland.esperandro.processor.PreferenceType;
import de.devland.esperandro.processor.Utils;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class GenericActionsGenerator {

    public static void createGenericActions(TypeSpec.Builder type, Collection<PreferenceInformation> allPreferences, boolean caching) throws IOException {

        MethodSpec.Builder get = MethodSpec.methodBuilder("get")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("android.content", "SharedPreferences"))
                .addStatement("return preferences");

        MethodSpec.Builder contains = MethodSpec.methodBuilder("contains")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addParameter(String.class, "key")
                .addStatement("return preferences.contains(key)");

        MethodSpec.Builder remove = MethodSpec.methodBuilder("remove")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(String.class, "key")
                .addStatement("preferences.edit().remove(key).$L", PreferenceEditorCommitStyle.APPLY.getStatementPart());

        MethodSpec.Builder registerListener = MethodSpec.methodBuilder("registerOnChangeListener")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.content", "SharedPreferences.OnSharedPreferenceChangeListener"), "listener")
                .addStatement("preferences.registerOnSharedPreferenceChangeListener(listener)");

        MethodSpec.Builder unregisterListener = MethodSpec.methodBuilder("unregisterOnChangeListener")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get("android.content", "SharedPreferences.OnSharedPreferenceChangeListener"), "listener")
                .addStatement("preferences.unregisterOnSharedPreferenceChangeListener(listener)");

        MethodSpec.Builder clear = MethodSpec.methodBuilder("clear")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("preferences.edit().clear().$L", PreferenceEditorCommitStyle.APPLY.getStatementPart());


        MethodSpec.Builder resetCache = MethodSpec.methodBuilder("resetCache")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("cache.evictAll()");

        MethodSpec clearDefined = createClearDefinedMethod(allPreferences, caching, remove, clear);

        MethodSpec initDefaults = createInitDefaultsMethod(allPreferences);


        type.addSuperinterface(SharedPreferenceActions.class)
                .addMethod(get.build())
                .addMethod(contains.build())
                .addMethod(remove.build())
                .addMethod(registerListener.build())
                .addMethod(unregisterListener.build())
                .addMethod(clear.build())
                .addMethod(clearDefined)
                .addMethod(initDefaults);

        if (caching) {
            type.addMethod(resetCache.build());
        }
    }

    private static MethodSpec createInitDefaultsMethod(Collection<PreferenceInformation> allPreferences) {
        MethodSpec.Builder initDefaultsBuilder = MethodSpec.methodBuilder("initDefaults")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("SharedPreferences.Editor editor = preferences.edit()");

        for (PreferenceInformation info : allPreferences) {
            if (info.getter != null) {
                String methodSuffix = Utils.getMethodSuffix(info.preferenceType.getPreferenceType());
                String value = "this." + info.preferenceName + "()";
                if (info.preferenceType.getPreferenceType() == PreferenceType.OBJECT) {
                    value = "Esperandro.getSerializer().serialize(" + value + ")";
                }

                initDefaultsBuilder.addStatement("editor.put$L($S, $L)", methodSuffix, info.preferenceName, value);
            } else {
                initDefaultsBuilder.addComment("no default initialization possible for '$L'", info.preferenceName);
            }
        }

        initDefaultsBuilder.addStatement("editor.commit()");
        return initDefaultsBuilder.build();
    }

    private static MethodSpec createClearDefinedMethod(Collection<PreferenceInformation> allPreferences, boolean caching, MethodSpec.Builder remove, MethodSpec.Builder clear) {
        MethodSpec.Builder clearDefinedBuilder = MethodSpec.methodBuilder("clearDefined")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("SharedPreferences.Editor editor = preferences.edit()");


        for (PreferenceInformation info : allPreferences) {
            clearDefinedBuilder.addStatement("editor.remove($S)", info.preferenceName);
        }

        if (caching) {
            remove.addStatement("cache.remove(key)");
            clear.addStatement("cache.evictAll()");
            for (PreferenceInformation info : allPreferences) {
                clearDefinedBuilder.addStatement("cache.remove($S)", info.preferenceName);
            }
        }

        return clearDefinedBuilder
                .addStatement("editor.$L", PreferenceEditorCommitStyle.APPLY.getStatementPart())
                .build();
    }
}
