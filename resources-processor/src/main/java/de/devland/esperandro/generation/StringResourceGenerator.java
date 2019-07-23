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

package de.devland.esperandro.generation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import de.devland.esperandro.StringResourceProcessor;
import de.devland.esperandro.annotations.GenerateStringResources;
import de.devland.esperandro.base.preferences.PreferenceInterface;
import de.devland.esperandro.base.processing.Environment;
import de.devland.esperandro.base.processing.ProcessingMessager;

public class StringResourceGenerator {

    private static final String STRING_RESOURCES_HINT = "<!--generated by esperandro-->\n";


    public static void generateStringResources(ProcessingEnvironment processingEnv, String valuesLocation,
                                               PreferenceInterface allPreferences, GenerateStringResources generateAnnotation) throws IOException {
        File valuesDir;
        if (valuesLocation == null) {
            valuesDir = findValues(processingEnv);
        } else {
            valuesDir = new File(valuesLocation);
        }

        if (valuesDir != null && valuesDir.exists()) {
            Path stringsPath = Paths.get(valuesDir.getCanonicalPath(),
                    generateAnnotation.filePrefix() + Environment.currentElement.getSimpleName() + ".xml");
            if (Files.exists(stringsPath)) {
                List<String> lines = Files.readAllLines(stringsPath, StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    if (STRING_RESOURCES_HINT.startsWith(lines.get(0))) {
                        Files.deleteIfExists(stringsPath);
                    } else {
                        ProcessingMessager.get()
                                .emitError("File '" + stringsPath.toString() + "' was not generated by esperandro and " +
                                        "will therefore not get deleted.", Environment.currentElement);
                    }
                } else {
                    Files.deleteIfExists(stringsPath);
                }
            }

            final StringBuilder resFile = new StringBuilder(STRING_RESOURCES_HINT);
            resFile.append("<resources>\n");
            for (String preferenceName : allPreferences.getAllPreferences()) {
                resFile.append("    <string translatable=\"false\" name=\"")
                        .append(generateAnnotation.stringPrefix())
                        .append(preferenceName)
                        .append("\">")
                        .append(preferenceName)
                        .append("</string>\n");
            }
            resFile.append("</resources>");

            Files.write(stringsPath, resFile.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            ProcessingMessager.get()
                    .emitError("The resource directory could not be found automatically." +
                            " Please provide the option '" + StringResourceProcessor.OPTION_VALUES_DIR + "' in your build.gradle to point it " +
                            "to the res/values directory.", Environment.currentElement);
        }
    }

    // https://stackoverflow.com/a/37230331
    private static File findValues(ProcessingEnvironment processingEnv) {
        Filer filer = processingEnv.getFiler();

        try {
            JavaFileObject dummySourceFile = filer.createSourceFile("dummy" + System.currentTimeMillis());
            String dummySourceFilePath = dummySourceFile.toUri().toString();

            if (dummySourceFilePath.startsWith("file:")) {
                if (!dummySourceFilePath.startsWith("file://")) {
                    dummySourceFilePath = "file://" + dummySourceFilePath.substring("file:".length());
                }
            } else {
                dummySourceFilePath = "file://" + dummySourceFilePath;
            }

            URI cleanURI = new URI(dummySourceFilePath);
            File dummyFile = new File(cleanURI);

            File projectRoot = dummyFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();

            return new File(projectRoot.getAbsolutePath() + "/src/main/res/values");
        } catch (Exception e) {
            return null;
        }
    }
}