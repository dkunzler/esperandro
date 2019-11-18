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

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class ProcessingMessager {

    private static final String MESSAGE_PREFIX = "esperandro: ";

    private static ProcessingMessager INSTANCE;
    private Messager messager;

    public static ProcessingMessager get() {
        return INSTANCE;
    }

    public static ProcessingMessager init(ProcessingEnvironment processingEnv) {
        INSTANCE = new ProcessingMessager(processingEnv);
        return INSTANCE;
    }

    public static void deinit() {
        INSTANCE.messager = null;
        INSTANCE = null;
    }

    private ProcessingMessager(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
    }

    public void emitError(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, MESSAGE_PREFIX + message, element);
    }

    public void emitWarning(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.WARNING, MESSAGE_PREFIX + message, element);
    }

    public void emitMissingDefaultWarning(String type, Element element) {
        messager.printMessage(Diagnostic.Kind.WARNING, MESSAGE_PREFIX + "Wrong or no mandatory overwritten default "
                + type + " value " +
                "detected, please check the annotation.", element);
    }
}
