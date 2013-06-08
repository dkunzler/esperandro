package de.devland.esperandro.processor;

import com.squareup.java.JavaWriter;
import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.SharedPreferenceMode;
import de.devland.esperandro.annotations.Default;
import de.devland.esperandro.annotations.SharedPreferences;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

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
// TODO errorHandling
// TODO implement other actions
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("de.devland.esperandro.annotations.SharedPreferences")
public class EsperandroAnnotationProcessor extends AbstractProcessor {

    public static final String SUFFIX = "$$Impl";
    public static final String[] neededImports = new String[]{"android.content.Context",
            "android.content.SharedPreferences", "android.preference.PreferenceManager", "java.util.Set"};
    public static final String sharedPreferencesAnnotationName = "de.devland.esperandro.annotations" + "" +
            ".SharedPreferences";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //assert (annotations.size() == 1);

        for (TypeElement typeElement : annotations) {
            if (typeElement.getQualifiedName().toString().equals(sharedPreferencesAnnotationName)) {
                Set<? extends Element> interfaces = roundEnv.getElementsAnnotatedWith(SharedPreferences.class);

                for (Element interfaze : interfaces) {
                    assert (interfaze.getKind() == ElementKind.INTERFACE);

                    try {
                        JavaWriter writer = initImplementation(interfaze);
                        List<? extends Element> potentialMethods = interfaze.getEnclosedElements();
                        for (Element element : potentialMethods) {
                            if (element.getKind() == ElementKind.METHOD) {
                                ExecutableElement method = (ExecutableElement) element;
                                if (isPutter(method)) {
                                    createPutter(method, writer);
                                } else if (isGetter(method)) {
                                    createGetter(method, writer);
                                } else {
                                    emitWarning("No getter or setter for preference detected.", method);
                                }
                            }
                        }
                        createGenericActions(writer);
                        finish(writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return false;
    }

    private void createGenericActions(JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.beginMethod("android.content.SharedPreferences", "get", Modifier.PUBLIC);
        writer.emitStatement("return preferences");
        writer.endMethod();
        writer.emitEmptyLine();

        writer.emitAnnotation(Override.class);
        writer.beginMethod("boolean", "contains", Modifier.PUBLIC, String.class.getName(), "key");
        writer.emitStatement("return preferences.contains(key)");
        writer.endMethod();
        writer.emitEmptyLine();

        writer.emitAnnotation(Override.class);
        writer.beginMethod("void", "registerOnChangeListener", Modifier.PUBLIC, "android.content.SharedPreferences" +
                ".OnSharedPreferenceChangeListener", "listener");
        writer.emitStatement("preferences.registerOnSharedPreferenceChangeListener");
        writer.endMethod();
        writer.emitEmptyLine();

        writer.emitAnnotation(Override.class);
        writer.beginMethod("void", "unregisterOnChangeListener", Modifier.PUBLIC, "android.content.SharedPreferences" +
                ".OnSharedPreferenceChangeListener", "listener");
        writer.emitStatement("preferences.unregisterOnSharedPreferenceChangeListener");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitWarning(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
    }

    private void finish(JavaWriter writer) throws IOException {
        writer.endType();
        writer.close();
    }

    private boolean isGetter(ExecutableElement method) {
        boolean isGetter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();

        if ((parameters == null || parameters.size() == 0) && PreferenceType.toPreferenceType(returnType) !=
                PreferenceType.NONE) {
            isGetter = true;
        }
        return isGetter;
    }

    private void createGetter(ExecutableElement method, JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        PreferenceType preferenceType = PreferenceType.toPreferenceType(method.getReturnType());
        writer.beginMethod(preferenceType.getTypeName(), method.getSimpleName().toString(), Modifier.PUBLIC);

        Default defaultAnnotation = method.getAnnotation(Default.class);
        boolean hasDefault = defaultAnnotation != null;
        String statementPattern = "preferences.get%s(\"%s\", %s)";
        String valueName = method.getSimpleName().toString();
        String methodSuffix = "";
        String defaultValue = "";
        switch (preferenceType) {
            case INT:
                methodSuffix = "Int";
                if (hasDefault && defaultAnnotation.ofInt() == Default.intDefault) {
                    emitMissingDefaultWarning("int", method);
                }
                defaultValue = hasDefault ? String.valueOf(defaultAnnotation.ofInt()) : String.valueOf(Default
                        .intDefault);
                break;
            case LONG:
                methodSuffix = "Long";
                if (hasDefault && defaultAnnotation.ofLong() == Default.longDefault) {
                    emitMissingDefaultWarning("long", method);
                }
                defaultValue = hasDefault ? String.valueOf(defaultAnnotation.ofLong()) : String.valueOf(Default
                        .longDefault);
                break;
            case FLOAT:
                methodSuffix = "Float";
                if (hasDefault && defaultAnnotation.ofFloat() == Default.floatDefault) {
                    emitMissingDefaultWarning("float", method);
                }
                defaultValue = hasDefault ? String.valueOf(defaultAnnotation.ofFloat()) : String.valueOf(Default
                        .floatDefault);
                break;
            case BOOLEAN:
                methodSuffix = "Boolean";
                if (hasDefault && defaultAnnotation.ofBoolean() == Default.booleanDefault) {
                    emitMissingDefaultWarning("boolean", method);
                }
                defaultValue = hasDefault ? String.valueOf(defaultAnnotation.ofBoolean()) : String.valueOf(Default
                        .booleanDefault);
                break;
            case STRING:
                if (hasDefault && defaultAnnotation.ofString().equals(Default.stringDefault)) {
                    emitMissingDefaultWarning("String", method);
                }
                methodSuffix = "String";
                defaultValue = (hasDefault ? ("\"" + defaultAnnotation.ofString() + "\"") : ("\"" + Default
                        .stringDefault + "\""));
                break;
            case STRINGSET:
                emitWarning("No default for Set<String> preferences allowed.", method);
                methodSuffix = "StringSet";
                defaultValue = "null";
                break;
        }

        String statement = String.format(statementPattern, methodSuffix, valueName, defaultValue);
        writer.emitStatement("return %s", statement);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitMissingDefaultWarning(String type, ExecutableElement method) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "No overwritten default " + type + " value " +
                "detected, please check the annotation.", method);
    }

    private void createPutter(ExecutableElement method, JavaWriter writer) throws IOException {
        writer.emitAnnotation(Override.class);
        TypeMirror parameterType = method.getParameters().get(0).asType();
        PreferenceType preferenceType = PreferenceType.toPreferenceType(parameterType);
        writer.beginMethod("void", method.getSimpleName().toString(), Modifier.PUBLIC, preferenceType.getTypeName(),
                method.getSimpleName().toString());
        String statementPattern = "preferences.edit().put%s(\"%s\", %s).commit()";
        String valueName = method.getSimpleName().toString();
        String methodSuffix = "";
        switch (preferenceType) {
            case INT:
                methodSuffix = "Int";
                break;
            case LONG:
                methodSuffix = "Long";
                break;
            case FLOAT:
                methodSuffix = "Float";
                break;
            case BOOLEAN:
                methodSuffix = "Boolean";
                break;
            case STRING:
                methodSuffix = "String";
                break;
            case STRINGSET:
                methodSuffix = "StringSet";
                break;
        }

        String statement = String.format(statementPattern, methodSuffix, valueName, valueName);
        writer.emitStatement(statement);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private boolean isPutter(ExecutableElement method) {
        boolean isPutter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();
        if (parameters != null && parameters.size() == 1 && returnTypeKind.equals(TypeKind.VOID) && PreferenceType
                .toPreferenceType(parameters.get(0).asType()) != PreferenceType.NONE) {
            isPutter = true;
        }
        return isPutter;
    }

    private JavaWriter initImplementation(Element interfaze) {
        JavaWriter result;
        SharedPreferences prefAnnotation = interfaze.getAnnotation(SharedPreferences.class);
        String preferencesName = prefAnnotation.name();
        SharedPreferenceMode mode = prefAnnotation.mode();

        Filer filer = processingEnv.getFiler();

        try {
            QualifiedNameable qualifiedNameable = (QualifiedNameable) interfaze;
            JavaFileObject jfo = filer.createSourceFile(qualifiedNameable.getQualifiedName() + SUFFIX);
            Writer writer = jfo.openWriter();
            result = new JavaWriter(writer);
            String[] split = qualifiedNameable.getQualifiedName().toString().split("\\.");
            String packageName = "";
            String typeName = split[split.length - 1];
            for (int i = 0; i < split.length - 1; i++) {
                packageName += split[i];
                if (i < split.length - 2) {
                    packageName += ".";
                }
            }
            result.emitPackage(packageName);
            result.emitImports(neededImports);
            result.emitEmptyLine();
            result.beginType(typeName + SUFFIX, "class", Modifier.PUBLIC, null, qualifiedNameable.getQualifiedName()
                    .toString(), SharedPreferenceActions.class.getName());
            result.emitEmptyLine();
            result.emitField("android.content.SharedPreferences", "preferences", Modifier.PRIVATE);


            result.emitEmptyLine();
            result.beginMethod(null, qualifiedNameable.getQualifiedName().toString() + SUFFIX, Modifier.PUBLIC,
                    "Context", "context");
            if (preferencesName != null && !preferencesName.equals("")) {
                result.emitStatement("this.preferences = context.getSharedPreferences(\"%s\", %s)", preferencesName,
                        mode.getSharedPreferenceModeStatement());
            } else {
                result.emitStatement("this.preferences = PreferenceManager.getDefaultSharedPreferences(context)");
            }
            result.endMethod();
            result.emitEmptyLine();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public enum PreferenceType {
        NONE(null, null), INT(TypeKind.INT), LONG(TypeKind.LONG), FLOAT(TypeKind.FLOAT), BOOLEAN(TypeKind.BOOLEAN),
        STRING(TypeKind.DECLARED, "java.lang.String"), STRINGSET(TypeKind.DECLARED, "java.util.Set<java.lang.String>");
        private final TypeKind typeKind;
        private final String declaredTypeName;

        PreferenceType(TypeKind typeKind) {
            this.typeKind = typeKind;
            this.declaredTypeName = null;
        }

        PreferenceType(TypeKind typeKind, String declaredTypeName) {
            this.typeKind = typeKind;
            this.declaredTypeName = declaredTypeName;
        }

        public static PreferenceType toPreferenceType(TypeMirror typeMirror) {
            PreferenceType type = NONE;

            switch (typeMirror.getKind()) {
                case BOOLEAN:
                    type = BOOLEAN;
                    break;
                case INT:
                    type = INT;
                    break;
                case LONG:
                    type = LONG;
                case FLOAT:
                    type = FLOAT;
                    break;
                case DECLARED:
                    if (typeMirror.toString().equals(PreferenceType.STRING.declaredTypeName)) {
                        type = STRING;
                    } else if (typeMirror.toString().equals(PreferenceType.STRINGSET.declaredTypeName)) {
                        type = STRINGSET;
                    }
            }

            return type;
        }

        public String getTypeName() {
            String typeName = declaredTypeName;
            switch (this) {
                case INT:
                    typeName = "int";
                    break;
                case LONG:
                    typeName = "long";
                    break;
                case FLOAT:
                    typeName = "float";
                    break;
                case BOOLEAN:
                    typeName = "boolean";
                    break;
            }
            return typeName;
        }
    }

}
