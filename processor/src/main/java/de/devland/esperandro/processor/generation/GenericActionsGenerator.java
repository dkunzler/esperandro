package de.devland.esperandro.processor.generation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collection;

import javax.lang.model.element.Modifier;

import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.processor.PreferenceInformation;

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

        MethodSpec.Builder clearDefinedBuilder = MethodSpec.methodBuilder("clearDefined")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("SharedPreferences.Editor editor = preferences.edit()");

        MethodSpec.Builder resetCache = MethodSpec.methodBuilder("resetCache")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("cache.evictAll()");


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

        MethodSpec clearDefined = clearDefinedBuilder
                .addStatement("editor.$L", PreferenceEditorCommitStyle.APPLY.getStatementPart())
                .build();

        MethodSpec.Builder initDefaultsBuilder = MethodSpec.methodBuilder("initDefaults")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);

        for (PreferenceInformation info : allPreferences) {
            if ((info.setter != null || info.commitSetter != null) && info.getter != null) {
                initDefaultsBuilder.addStatement("this.$L(this.$L())", info.preferenceName, info.preferenceName);
            }
        }


        type.addSuperinterface(SharedPreferenceActions.class)
                .addMethod(get.build())
                .addMethod(contains.build())
                .addMethod(remove.build())
                .addMethod(registerListener.build())
                .addMethod(unregisterListener.build())
                .addMethod(clear.build())
                .addMethod(clearDefined)
                .addMethod(initDefaultsBuilder.build());

        if (caching) {
            type.addMethod(resetCache.build());
        }
    }
}
