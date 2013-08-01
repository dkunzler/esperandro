package de.devland.esperandro.processor;

import com.squareup.java.JavaWriter;
import de.devland.esperandro.SharedPreferenceActions;
import de.devland.esperandro.SharedPreferenceMode;
import de.devland.esperandro.annotations.SharedPreferences;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("de.devland.esperandro.annotations.SharedPreferences")
public class EsperandroAnnotationProcessor extends AbstractProcessor {

    public static final String SUFFIX = "$$Impl";
    public static final String[] neededImports = new String[]{"android.content.Context",
            "android.content.SharedPreferences", "android.preference.PreferenceManager", "java.util.Set"};
    public static final String sharedPreferencesAnnotationName = "de.devland.esperandro.annotations" + "" +
            ".SharedPreferences";

    Warner warner;
    Getter getter;
    Putter putter;


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        warner = new Warner(processingEnv);
        getter = new Getter(warner);
        putter = new Putter();


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
                                if (putter.isPutter(method)) {
                                    putter.createPutter(method, writer);
                                } else if (getter.isGetter(method)) {
                                    getter.createGetter(method, writer);
                                } else {
                                    warner.emitWarning("No getter or setter for preference detected.", method);
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
        writer.emitStatement("preferences.registerOnSharedPreferenceChangeListener(listener)");
        writer.endMethod();
        writer.emitEmptyLine();

        writer.emitAnnotation(Override.class);
        writer.beginMethod("void", "unregisterOnChangeListener", Modifier.PUBLIC, "android.content.SharedPreferences"
                + ".OnSharedPreferenceChangeListener", "listener");
        writer.emitStatement("preferences.unregisterOnSharedPreferenceChangeListener(listener)");
        writer.endMethod();
        writer.emitEmptyLine();

        writer.emitAnnotation(Override.class);
        writer.beginMethod("void", "clear", Modifier.PUBLIC);
        writer.emitStatement("preferences.edit().clear().commit()");
        writer.endMethod();
        writer.emitEmptyLine();
    }


    private void finish(JavaWriter writer) throws IOException {
        writer.endType();
        writer.close();
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
                    break;
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
