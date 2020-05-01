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

package de.devland.esperandro.base.processing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import de.devland.esperandro.annotations.Cached;
import de.devland.esperandro.annotations.SharedPreferences;
import de.devland.esperandro.base.Constants;
import de.devland.esperandro.base.MethodAnalyzer;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.PreferenceInterface;

@SupportedAnnotationTypes("de.devland.esperandro.annotations.SharedPreferences")
public abstract class AbstractEsperandroProcessor extends AbstractProcessor {

    protected ProcessingMessager messager;
    private final Map<TypeMirror, Element> rootElements = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        preProcessEnvironment(roundEnv);

        for (TypeElement typeElement : annotations) {
            if (typeElement.getQualifiedName().toString().equals(Constants.SHARED_PREFERENCES_ANNOTATION_NAME)) {
                Set<? extends Element> interfaces = roundEnv.getElementsAnnotatedWith(SharedPreferences.class);
                messager = ProcessingMessager.init(processingEnv);

                for (Element interfaze : interfaces) {
                    // fix some weird behaviour where getElementsAnnotatedWith returns more elements than expected
                    if (interfaze.getKind() == ElementKind.INTERFACE && interfaze.getAnnotation(SharedPreferences
                            .class) != null) {
                        try {
                            // collect all PreferenceInformation
                            PreferenceInterface allPreferences = analyze(interfaze, interfaze);
                            Environment.currentElement = interfaze;
                            Environment.currentPreferenceInterface = allPreferences;
                            generate(interfaze, allPreferences);
                            Environment.currentElement = null;
                            Environment.currentPreferenceInterface = null;
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            e.printStackTrace(pw);
                            String stacktrace = sw.toString().replace("\n", " - ");
                            messager.emitError("Esperandro Processor Error: " + stacktrace, interfaze);
                            throw new RuntimeException(e);
                        }
                    }
                }

                ProcessingMessager.deinit();
            }
        }

        // do not return true because we have potentially multiple processors
        return false;
    }

    private PreferenceInterface analyze(Element topLevelInterface, Element currentInterface) {
        PreferenceInterface preferences =
                new PreferenceInterface(topLevelInterface.getAnnotation(SharedPreferences.class),
                        topLevelInterface.getAnnotation(Cached.class));
        analyze(topLevelInterface, currentInterface, preferences);
        return preferences;
    }

    private void analyze(Element topLevelInterface, Element currentInterface, PreferenceInterface preferences) {
        List<? extends Element> potentialMethods = currentInterface.getEnclosedElements();
        for (Element element : potentialMethods) {
            if (element.getKind() == ElementKind.METHOD
                    && !element.getModifiers().contains(Modifier.STATIC)) {
                ExecutableElement method = (ExecutableElement) element;

                MethodInformation methodInformation = MethodInformation.from(method);

                if (getMethodAnalyzer().isApplicableMethod(methodInformation)) {
                    String preferenceName = getMethodAnalyzer().getPreferenceName(methodInformation);
                    preferences.addMethod(preferenceName, methodInformation);
                    preferences.addTypeInformation(preferenceName, getMethodAnalyzer().getPreferenceType(methodInformation));
                }
            }
        }

        // recursively analyze all super interfaces
        List<? extends TypeMirror> interfaces = ((TypeElement) currentInterface).getInterfaces();
        for (TypeMirror superInterfaceType : interfaces) {
            Element subInterface = rootElements.get(superInterfaceType);
            String subInterfaceTypeName = superInterfaceType.toString();
            if (!Constants.SUPER_INTERFACE_BLACKLIST.contains(subInterfaceTypeName)) {
                if (subInterface != null) {
                    analyze(topLevelInterface, subInterface, preferences);
                } else {
                    messager.emitError("Could not load Interface '" + subInterfaceTypeName + "' for generation.",
                            topLevelInterface);
                }
            }
        }
    }

    private MethodAnalyzer getMethodAnalyzer() {
        List<? extends MethodAnalyzer> methodAnalyzers = getMethodAnalyzers();
        if (methodAnalyzers == null || methodAnalyzers.isEmpty()) {
            methodAnalyzers = Collections.singletonList((MethodAnalyzer) new SimpleMethodAnalyzer());
        }

        return new ChainedMethodAnalyzer(methodAnalyzers);
    }

    protected abstract List<? extends MethodAnalyzer> getMethodAnalyzers();

    protected abstract void generate(Element currentElement, PreferenceInterface preferenceInterface) throws Exception;

    private void preProcessEnvironment(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            rootElements.put(element.asType(), element);
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
