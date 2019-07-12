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

import java.util.Collections;
import java.util.List;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;

import de.devland.esperandro.annotations.experimental.GenerateStringResources;
import de.devland.esperandro.base.MethodAnalyzer;
import de.devland.esperandro.base.preferences.PreferenceInterface;
import de.devland.esperandro.base.processing.AbstractEsperandroProcessor;
import de.devland.esperandro.generation.StringResourceGenerator;

@SupportedAnnotationTypes("de.devland.esperandro.annotations.SharedPreferences")
@SupportedOptions(StringResourceProcessor.OPTION_VALUES_DIR)
public class StringResourceProcessor extends AbstractEsperandroProcessor {

    public static final String OPTION_VALUES_DIR = "esperandro_valuesDir";

    @Override
    protected List<? extends MethodAnalyzer> getMethodAnalyzers() {
        return Collections.singletonList(new NameMethodAnalyzer());
    }

    @Override
    protected void generate(Element currentElement, PreferenceInterface preferenceInterface) throws Exception {
        GenerateStringResources generateStringResources = currentElement.getAnnotation(GenerateStringResources.class);
        if (generateStringResources != null) {
            StringResourceGenerator.generateStringResources(processingEnv,
                    processingEnv.getOptions().get(OPTION_VALUES_DIR), preferenceInterface,
                    generateStringResources);
        }
    }
}
