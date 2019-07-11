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
 */

package de.devland.esperandro;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import de.devland.esperandro.analysis.AdderAnalyzer;
import de.devland.esperandro.analysis.GeneratorAwareAnalyzer;
import de.devland.esperandro.analysis.GetterAnalyzer;
import de.devland.esperandro.analysis.PutterAnalyzer;
import de.devland.esperandro.analysis.RemoverAnalyzer;
import de.devland.esperandro.analysis.RuntimeDefaultGetterAnalyzer;
import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.PreferenceInterface;
import de.devland.esperandro.base.preferences.TypeInformation;
import de.devland.esperandro.base.processing.AbstractEsperandroProcessor;
import de.devland.esperandro.generation.ContainerClassGenerator;
import de.devland.esperandro.generation.GenericActionsGenerator;
import de.devland.esperandro.generation.PreferenceClassConstructorGenerator;
import java8.util.stream.StreamSupport;

@SupportedAnnotationTypes("de.devland.esperandro.annotations.SharedPreferences")
public class PreferenceProcessor extends AbstractEsperandroProcessor {
    @Override
    protected List<GeneratorAwareAnalyzer> getMethodAnalyzers() {
        return Arrays.asList(new GetterAnalyzer(), new PutterAnalyzer(),
                new RuntimeDefaultGetterAnalyzer(), new AdderAnalyzer(), new RemoverAnalyzer());
    }

    @Override
    protected void generate(Element currentElement, PreferenceInterface preferenceInterface) throws Exception {
        Cached cacheAnnotation = preferenceInterface.getCacheAnnotation();
        TypeSpec.Builder type = initImplementation(currentElement, cacheAnnotation);
        PreferenceClassConstructorGenerator.createConstructor(type, preferenceInterface);
        PreferenceClassConstructorGenerator.createDefaultConstructor(type, preferenceInterface);
        addMissingRequiredMethods(preferenceInterface);
        createMethods(type, preferenceInterface);
        GenericActionsGenerator.createGenericActions(type, preferenceInterface);
        ContainerClassGenerator.createGenericClassImplementations(type, preferenceInterface);
        finish(currentElement, type);
    }

    private TypeSpec.Builder initImplementation(Element interfaze, Cached cacheAnnotation) {
        TypeSpec.Builder result;
        SharedPreferences prefAnnotation = interfaze.getAnnotation(SharedPreferences.class);
        String preferencesName = prefAnnotation.name();
        if (cacheAnnotation != null && preferencesName.equals("")) {
            messager.emitWarning("Caching should not be used on default SharedPreferences. This is not officially supported.", interfaze);
        }

        try {
            String typeName = Utils.classNameFromInterface(interfaze) + Constants.IMPLEMENTATION_SUFFIX;
            result = TypeSpec.classBuilder(typeName)
                    .addSuperinterface(TypeName.get(interfaze.asType()))
                    .addField(ClassName.get("android.content", "SharedPreferences"), "preferences", Modifier.PRIVATE, Modifier.FINAL);
            if (Utils.isPublic(interfaze)) {
                result.addModifiers(Modifier.PUBLIC);
            }
            if (cacheAnnotation != null) {
                result.addSuperinterface(CacheActions.class);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void addMissingRequiredMethods(PreferenceInterface preferenceInterface) {
        for (String preferenceName : preferenceInterface.getAllPreferences()) {
            if (!GetterAnalyzer.hasMatch(preferenceInterface.getMethodsForPreference(preferenceName))) {
                preferenceInterface.addMethod(preferenceName, MethodInformation.internalMethod(preferenceInterface.getTypeOfPreference(preferenceName), null));
            }
            if (!PutterAnalyzer.hasMatch(preferenceInterface.getMethodsForPreference(preferenceName))) {
                preferenceInterface.addMethod(preferenceName, MethodInformation.internalMethod(TypeInformation.from(void.class), preferenceInterface.getTypeOfPreference(preferenceName)));
            }
        }
    }

    private void createMethods(TypeSpec.Builder type, PreferenceInterface preferenceInterface) {
        StreamSupport.stream(preferenceInterface.getAllPreferences())
                .flatMap(name -> StreamSupport.stream(preferenceInterface.getMethodsForPreference(name)))
                .forEach(method -> StreamSupport.stream(getMethodAnalyzers())
                        .filter(analyzer -> analyzer.isApplicableMethod(method))
                        .map(GeneratorAwareAnalyzer::getGenerator)
                        .forEach(generator -> generator.generateMethod(type, method, preferenceInterface.getCacheAnnotation())));
    }

    private void finish(Element interfaze, TypeSpec.Builder type) throws IOException {
        String packageName = Utils.packageNameFromInterface(interfaze);

        JavaFile javaFile = JavaFile.builder(packageName, type.build())
                .build();
        Filer filer = processingEnv.getFiler();
        javaFile.writeTo(filer);
    }
}
