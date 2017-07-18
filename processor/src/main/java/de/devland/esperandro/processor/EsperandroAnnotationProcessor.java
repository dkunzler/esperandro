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

import com.squareup.javapoet.*;
import de.devland.esperandro.CacheActions;
import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.SharedPreferenceMode;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.annotations.experimental.Cached;
import de.devland.esperandro.annotations.experimental.GenerateStringResources;
import de.devland.esperandro.processor.generation.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// TODO errorHandling
// TODO apply or commit as annotation
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("de.devland.esperandro.annotations.SharedPreferences")
@SupportedOptions(EsperandroAnnotationProcessor.OPTION_RESDIR)
public class EsperandroAnnotationProcessor extends AbstractProcessor {

    public static final String OPTION_RESDIR = "esperandro_resDir";

    private Warner warner;
    private Map<TypeMirror, Element> rootElements;
    private String resDirLocation;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        resDirLocation = processingEnv.getOptions().get(OPTION_RESDIR);
    }

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
                            // collect all PreferenceInformation
                            Collection<PreferenceInformation> allPreferences = analyze(interfaze, interfaze);

                            // add interfaze as element for every method without explicit element for warnings
                            for (PreferenceInformation info : allPreferences) {
                                if (info.adderElement == null) info.adderElement = interfaze;
                                if (info.removerElement == null) info.removerElement = interfaze;
                                if (info.getterElement == null) info.getterElement = interfaze;
                                if (info.setterElement == null) info.setterElement = interfaze;
                                if (info.runtimeDefaultGetterElement == null) info.runtimeDefaultGetterElement = interfaze;
                            }
                            // TODO check all types
//                            TODO if (hasErrors(allPreferences)) {
//                                return false; // do not generate anything if errors are there
//                            }
                            // handle errors and warning, check type for adder/remover
//                            TODO generateWarning(allPreferences);
                            // generate implementations
                            // generate string resources
                            // generate java statics
                            Cached cacheAnnotation = interfaze.getAnnotation(Cached.class);
                            boolean caching = cacheAnnotation != null;
                            TypeSpec.Builder type = initImplementation(interfaze, cacheAnnotation);

                            createMethods(type, allPreferences, cacheAnnotation);
                            GenericActionsGenerator.createGenericActions(type, allPreferences, caching);
                            createGenericClassImplementations(type, allPreferences);
                            createDefaultConstructor(type, allPreferences, cacheAnnotation);
                            finish(interfaze, allPreferences, type);


                            // reinitialize getterGenerator and putter to start fresh for each interface
//                            getterGenerator = new GetterGenerator(warner);
//                            putterGenerator = new PutterGenerator();
//                            processInterfaceMethods(interfaze, interfaze, type, cacheAnnotation);
//                            checkPreferenceKeys();
//                        } catch (IOException e) {
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            e.printStackTrace(pw);
                            String stacktrace = sw.toString().replace("\n", " - ");
                            warner.emitError("Processor Error: " + stacktrace, interfaze);
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }


        return false;
    }

    private void createMethods(TypeSpec.Builder type, Collection<PreferenceInformation> allPreferences, Cached cachedAnnotation) {
        // reinitialize getterGenerator and putter to start fresh for each interface
        GetterGenerator getterGenerator = new GetterGenerator(warner);
        PutterGenerator putterGenerator = new PutterGenerator();
        // TODO AdderGenerator, RemoverGenerator

        for (PreferenceInformation info : allPreferences) {
            if (info.hasSetter || info.hasCommitSetter) {
                putterGenerator.create(type, info, cachedAnnotation, info.hasCommitSetter);
            }
            if (info.hasGetter) {
                getterGenerator.create(type, info, cachedAnnotation, false);
            }
            if (info.hasRuntimeDefaultGetter) {
                getterGenerator.create(type, info, cachedAnnotation, true);
            }
        }
    }

    private void createGenericClassImplementations(TypeSpec.Builder type, Collection<PreferenceInformation> allPreferences) throws IOException {
        for (PreferenceInformation info : allPreferences) {
            if (info.typeInformation.isGeneric()) {
                TypeSpec innerGenericType = TypeSpec.classBuilder(Utils.createClassNameForPreference(info.preferenceName))
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addSuperinterface(Serializable.class)
                        .addField(info.typeInformation.getType(), "value", Modifier.PUBLIC)
                        .build();

                type.addType(innerGenericType);
            }
        }
    }

    private void preProcessEnvironment(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            rootElements.put(element.asType(), element);
        }
    }

    private Collection<PreferenceInformation> analyze(Element topLevelInterface, Element currentInterface) {
        Map<String, PreferenceInformation> informationByType = new HashMap<>();
        analyze(topLevelInterface, currentInterface, informationByType);
        return informationByType.values();
    }

    private void analyze(Element topLevelInterface, Element currentInterface, Map<String, PreferenceInformation> informationByType) {
        List<? extends Element> potentialMethods = currentInterface.getEnclosedElements();
        for (Element element : potentialMethods) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                String preferenceName = PreferenceClassifier.preferenceName(method);
                PreferenceInformation info = informationByType.get(preferenceName);
                if (info == null) {
                    info = new PreferenceInformation();
                    info.preferenceName = preferenceName;
                    informationByType.put(preferenceName, info);
                }
//                try {
                    PreferenceClassifier.analyze(method, info);
//                } catch (MethodException e) {
//                    warner.emitError("No valid method name detected.", method);
//                }
            }
        }

        List<? extends TypeMirror> interfaces = ((TypeElement) currentInterface).getInterfaces();
        for (TypeMirror subInterfaceType : interfaces) {
            Element subInterface = rootElements.get(subInterfaceType);
            String subInterfaceTypeName = subInterfaceType.toString();
            if (!subInterfaceTypeName.equals(SharedPreferenceActions.class.getName()) &&
                    !subInterfaceTypeName.equals(CacheActions.class.getName())) {
                if (subInterface != null) {
                    analyze(topLevelInterface, subInterface, informationByType);
                } else {
                    try {
                        Class<?> subInterfaceClass = Class.forName(subInterfaceTypeName);
                        analyze(topLevelInterface, subInterfaceClass, informationByType);
                    } catch (ClassNotFoundException e) {
                        warner.emitError("Could not load Interface '" + subInterfaceTypeName + "' for generation.",
                                topLevelInterface);
                    }
                }
            }
        }
    }

    private void analyze(Element topLevelInterface, Class<?> interfaceClass, Map<String, PreferenceInformation> informationByType) {
        for (Method method : interfaceClass.getDeclaredMethods()) {
            String preferenceName = PreferenceClassifier.preferenceName(method);
            PreferenceInformation info = informationByType.get(preferenceName);
            if (info == null) {
                info = new PreferenceInformation();
                info.preferenceName = preferenceName;
                informationByType.put(preferenceName, info);
            }
//            try {
                PreferenceClassifier.analyze(method, info);
//            } catch (MethodException e) {
//                warner.emitError("No valid method name detected in class '" + interfaceClass.getName() + "' for " +
//                        "method: '" + method.getName() + "'.", topLevelInterface);
//            }
        }

        for (Class<?> subInterfaceClass : interfaceClass.getInterfaces()) {
            if (subInterfaceClass.getName() != null && !subInterfaceClass.getName().equals(SharedPreferenceActions
                    .class.getName())) {
                analyze(topLevelInterface, subInterfaceClass, informationByType);
            }
        }
    }

    private TypeSpec.Builder initImplementation(Element interfaze, Cached cacheAnnotation) {
        TypeSpec.Builder result;
        SharedPreferences prefAnnotation = interfaze.getAnnotation(SharedPreferences.class);
        String preferencesName = prefAnnotation.name();
        SharedPreferenceMode mode = prefAnnotation.mode();
        if (cacheAnnotation != null && preferencesName.equals("")) {
            warner.emitWarning("Caching should not be used on default SharedPreferences. This is not officially supported.", interfaze);
        }

        try {
            QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
            boolean preferenceNamePresent = preferencesName != null && !preferencesName.equals("");
            String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
            String typeName = split[split.length - 1] + Constants.IMPLEMENTATION_SUFFIX;
            result = TypeSpec.classBuilder(typeName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(SharedPreferenceActions.class)
                    .addSuperinterface(TypeName.get(interfaze.asType()))
                    .addField(ClassName.get("android.content", "SharedPreferences"), "preferences", Modifier.PRIVATE, Modifier.FINAL);
            if (cacheAnnotation != null) {
                result.addSuperinterface(CacheActions.class);
            }
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("android.content", "Context"), "context");
            if (preferenceNamePresent) {
                constructor.addStatement("this.preferences = context.getSharedPreferences($S, $L)", preferencesName,
                        mode.getSharedPreferenceModeStatement());
            } else {
                constructor.addStatement("this.preferences = $T.getDefaultSharedPreferences(context)", ClassName.get("android.preference", "PreferenceManager"));
            }

            if (cacheAnnotation != null) {
                constructor.addParameter(TypeName.INT, "cacheSize");
                ClassName cacheClass;
                if (cacheAnnotation.support()) {
                    cacheClass = ClassName.get("android.support.v4.util", "LruCache");
                } else {
                    cacheClass = ClassName.get("android.util", "LruCache");
                }
                ParameterizedTypeName lruCache = ParameterizedTypeName.get(
                        cacheClass,
                        ClassName.get(String.class),
                        ClassName.get(Object.class));
                result.addField(lruCache, "cache", Modifier.PRIVATE, Modifier.FINAL);

                constructor.addStatement("cache = new LruCache<$T, $T>(cacheSize)", String.class, Object.class);
            }

            result.addMethod(constructor.build());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void createDefaultConstructor(TypeSpec.Builder type, Collection<PreferenceInformation> allPreferences, Cached cacheAnnotation) {
        if (cacheAnnotation != null) {
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("android.content", "Context"), "context");
            int cacheSize = cacheAnnotation.cacheSize();
            if (cacheAnnotation.autoSize()) {
                cacheSize = allPreferences.size();
            }
            constructor.addStatement("this(context, $L)", cacheSize);
            type.addMethod(constructor.build());
        }
    }

    private void finish(Element interfaze, Collection<PreferenceInformation> allPreferences, TypeSpec.Builder type) throws IOException {
        GenerateStringResources generateAnnotation = interfaze.getAnnotation(GenerateStringResources.class);
        QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
        String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
        String packageName = "";
        for (int i = 0; i < split.length - 1; i++) {
            packageName += split[i];
            if (i < split.length - 2) {
                packageName += ".";
            }
        }


        JavaFile javaFile = JavaFile.builder(packageName, type.build())
                .build();
        Filer filer = processingEnv.getFiler();
        javaFile.writeTo(filer);

        if (generateAnnotation != null) {
            StringResourceGenerator resourceGenerator = new StringResourceGenerator(resDirLocation, warner);
            resourceGenerator.generateStringResources(interfaze, allPreferences, generateAnnotation);
        }
    }

}
