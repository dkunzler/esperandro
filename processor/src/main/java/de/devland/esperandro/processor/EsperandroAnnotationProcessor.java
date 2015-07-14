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
 *
 */

package de.devland.esperandro.processor;

import android.content.Context;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.SharedPreferenceMode;
import de.devland.esperandro.annotations.SharedPreferences;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

// TODO errorHandling
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("de.devland.esperandro.annotations.SharedPreferences")
public class EsperandroAnnotationProcessor extends AbstractProcessor {

    private Warner warner;
    private GetterGenerator getterGenerator;
    private PutterGenerator putterGenerator;
    private Map<TypeMirror, Element> rootElements;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        warner = new Warner(processingEnv);
        rootElements = new HashMap<TypeMirror, Element>();

        preProcessEnvironment(roundEnv);


        for (TypeElement typeElement : annotations) {
            if (typeElement.getQualifiedName().toString().equals(Constants.SHARED_PREFERENCES_ANNOTATION_NAME)) {
                Set<? extends Element> interfaces = roundEnv.getElementsAnnotatedWith(SharedPreferences.class);

                for (Element interfaze : interfaces) {
                    // fix some weird behaviour where getElementsAnnotatedWith returns more elements than expected
                    if (interfaze.getKind() == ElementKind.INTERFACE && interfaze.getAnnotation(SharedPreferences
                            .class) != null) {
                        try {
                            // reinitialize getterGenerator and putter to start fresh for each interface
                            getterGenerator = new GetterGenerator(warner);
                            putterGenerator = new PutterGenerator();
                            TypeSpec.Builder writer = initImplementation(interfaze);
                            processInterfaceMethods(interfaze, interfaze, writer);
                            createGenericActions(writer);
                            createGenericClassImplementations(writer);
                            finish(interfaze, writer);
                            checkPreferenceKeys();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }


        return false;
    }

    private void createGenericClassImplementations(TypeSpec.Builder writer) throws IOException {
        for (String preferenceName : getterGenerator.getGenericTypeNames().keySet()) {
            TypeSpec innerGenericType = TypeSpec.classBuilder(preferenceName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addField(getterGenerator.getGenericTypeNames().get(preferenceName), "value", Modifier.PUBLIC)
                    .build();

            writer.addType(innerGenericType);
        }
    }

    private void preProcessEnvironment(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            rootElements.put(element.asType(), element);
        }
    }

    private void processInterfaceMethods(Element topLevelInterface, Element currentInterfaze,
                                         TypeSpec.Builder writer) throws IOException {
        List<? extends Element> potentialMethods = currentInterfaze.getEnclosedElements();
        for (Element element : potentialMethods) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                if (putterGenerator.isPutter(method)) {
                    putterGenerator.createPutterFromModel(method, writer);
                } else if (getterGenerator.isGetter(method)) {
                    getterGenerator.createGetterFromModel(method, writer);
                } else {
                    warner.emitError("No valid getter or setter detected.", method);
                }
            }
        }

        List<? extends TypeMirror> interfaces = ((TypeElement) currentInterfaze).getInterfaces();
        for (TypeMirror subInterfaceType : interfaces) {
            Element subInterface = rootElements.get(subInterfaceType);
            String subInterfaceTypeName = subInterfaceType.toString();
            if (!subInterfaceTypeName.equals(SharedPreferenceActions.class.getName())) {
                if (subInterface != null) {
                    processInterfaceMethods(topLevelInterface, subInterface, writer);
                } else {
                    try {
                        Class<?> subInterfaceClass = Class.forName(subInterfaceTypeName);
                        processInterfacesReflection(topLevelInterface, subInterfaceClass, writer);
                    } catch (ClassNotFoundException e) {
                        warner.emitError("Could not load Interface '" + subInterfaceTypeName + "' for generation.",
                                topLevelInterface);
                    }
                }
            }
        }
    }

    private void processInterfacesReflection(Element topLevelInterface, Class<?> interfaceClass,
                                             TypeSpec.Builder writer) throws IOException {

        for (Method method : interfaceClass.getDeclaredMethods()) {
            if (putterGenerator.isPutter(method)) {
                putterGenerator.createPutterFromReflection(method, topLevelInterface, writer);
            } else if (getterGenerator.isGetter(method)) {
                getterGenerator.createGetterFromReflection(method, topLevelInterface, writer);
            } else {
                warner.emitError("No valid getter or setter detected in class '" + interfaceClass.getName() + "' for " +
                        "method: '" + method.getName() + "'.", topLevelInterface);
            }
        }

        for (Class<?> subInterfaceClass : interfaceClass.getInterfaces()) {
            if (subInterfaceClass.getName() != null && !subInterfaceClass.getName().equals(SharedPreferenceActions
                    .class.getName())) {
                processInterfacesReflection(topLevelInterface, subInterfaceClass, writer);
            }
        }
    }

    private void checkPreferenceKeys() {
        for (String key : getterGenerator.getPreferenceKeys().keySet()) {
            if (!putterGenerator.getPreferenceKeys().containsKey(key)) {
                warner.emitWarning("No putter found for getter '" + key + "'", getterGenerator.getPreferenceKeys().get(key));
            }
        }

        for (String key : putterGenerator.getPreferenceKeys().keySet()) {
            if (!getterGenerator.getPreferenceKeys().containsKey(key)) {
                warner.emitWarning("No getterGenerator found for putter '" + key + "'", putterGenerator.getPreferenceKeys().get(key));
            }
        }
    }

    private TypeSpec.Builder initImplementation(Element interfaze) {
        TypeSpec.Builder result;
        SharedPreferences prefAnnotation = interfaze.getAnnotation(SharedPreferences.class);
        String preferencesName = prefAnnotation.name();
        SharedPreferenceMode mode = prefAnnotation.mode();

        try {
            QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
            boolean preferenceNamePresent = preferencesName != null && !preferencesName.equals("");
            String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
            String typeName = split[split.length - 1] + Constants.IMPLEMENTATION_SUFFIX;
            result = TypeSpec.classBuilder(typeName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(SharedPreferenceActions.class)
                    .addSuperinterface(TypeName.get(interfaze.asType()))
                    .addField(TypeName.get(android.content.SharedPreferences.class), "preferences", Modifier.PRIVATE, Modifier.FINAL);

            String preferenceStatement;
            if (preferenceNamePresent) {
                preferenceStatement = String.format("this.preferences = context.getSharedPreferences(\"%s\", %s)", preferencesName,
                        mode.getSharedPreferenceModeStatement());
            } else {
                preferenceStatement = "this.preferences = PreferenceManager.getDefaultSharedPreferences(context)";
            }

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addParameter(Context.class, "context")
                    .addStatement(preferenceStatement)
                    .build();

            result.addMethod(constructor);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    private void createGenericActions(TypeSpec.Builder type) throws IOException {

        MethodSpec get = MethodSpec.methodBuilder("get")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(android.content.SharedPreferences.class)
                .addStatement("return preferences")
                .build();

        MethodSpec contains = MethodSpec.methodBuilder("contains")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(boolean.class)
                .addParameter(String.class, "key")
                .addStatement("return preferences.contains(key)")
                .build();

        MethodSpec remove = MethodSpec.methodBuilder("remove")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(String.class, "key")
                .addStatement("preferences.edit().remove(key).%s", PreferenceEditorCommitStyle.APPLY.getStatementPart())
                .build();

        MethodSpec registerListener = MethodSpec.methodBuilder("registerOnChangeListener")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(android.content.SharedPreferences.OnSharedPreferenceChangeListener.class, "listener")
                .addStatement("preferences.registerOnSharedPreferenceChangeListener(listener)")
                .build();

        MethodSpec unregisterListener = MethodSpec.methodBuilder("unregisterOnChangeListener")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(android.content.SharedPreferences.OnSharedPreferenceChangeListener.class, "listener")
                .addStatement("preferences.unregisterOnSharedPreferenceChangeListener(listener)")
                .build();

        MethodSpec clear = MethodSpec.methodBuilder("clear")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("preferences.edit().clear().%s", PreferenceEditorCommitStyle.APPLY.getStatementPart())
                .build();

        MethodSpec.Builder clearDefinedBuilder = MethodSpec.methodBuilder("clearDefined")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addStatement("SharedPreferences.Editor editor = preferences.edit()");


        Set<String> preferenceNames = new LinkedHashSet<String>();
        preferenceNames.addAll(putterGenerator.getPreferenceKeys().keySet());
        preferenceNames.addAll(getterGenerator.getPreferenceKeys().keySet());
        for (String preferenceName : preferenceNames) {
            clearDefinedBuilder.addStatement("editor.remove(\"%s\")", preferenceName);
        }

        MethodSpec clearDefined = clearDefinedBuilder
                .addStatement("editor.%s", PreferenceEditorCommitStyle.APPLY.getStatementPart())
                .build();

        MethodSpec.Builder initDefaultsBuilder = MethodSpec.methodBuilder("initDefaults")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);


        for (String preferenceKey : getterGenerator.getPreferenceKeys().keySet()) {
            if (putterGenerator.getPreferenceKeys().containsKey(preferenceKey)) {
                clearDefinedBuilder.addStatement("this.%s(this.%s())", preferenceKey, preferenceKey);
            }
        }

        MethodSpec initDefaults = initDefaultsBuilder.build();

        type.addMethod(get)
                .addMethod(contains)
                .addMethod(remove)
                .addMethod(registerListener)
                .addMethod(unregisterListener)
                .addMethod(clear)
                .addMethod(clearDefined)
                .addMethod(initDefaults);
    }


    private void finish(Element interfaze, TypeSpec.Builder writer) throws IOException {
        QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
        String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
        String packageName = "";
        for (int i = 0; i < split.length - 1; i++) {
            packageName += split[i];
            if (i < split.length - 2) {
                packageName += ".";
            }
        }

        JavaFile javaFile = JavaFile.builder(packageName, writer.build())
                .build();
        Filer filer = processingEnv.getFiler();
        javaFile.writeTo(filer);
    }


}
