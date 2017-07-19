package de.devland.esperandro.processor.generation;

import de.devland.esperandro.annotations.experimental.GenerateStringResources;
import de.devland.esperandro.processor.EsperandroAnnotationProcessor;
import de.devland.esperandro.processor.PreferenceInformation;
import de.devland.esperandro.processor.Warner;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * @author David Kunzler on 18.07.2017.
 */
public class StringResourceGenerator {

    private static final String STRING_RESOURCES_HINT = "<!--generated by esperandro-->\n";

    private final String valuesLocation;
    private ProcessingEnvironment processingEnv;
    private final Warner warner;

    public StringResourceGenerator(String valuesLocation, ProcessingEnvironment processingEnv, Warner warner) {
        this.valuesLocation = valuesLocation;
        this.processingEnv = processingEnv;
        this.warner = warner;
    }


    public void generateStringResources(Element interfaze, Collection<PreferenceInformation> allPreferences, GenerateStringResources generateAnnotation) throws IOException {
        File valuesDir;
        if (valuesLocation == null) {
            valuesDir = findValues();
        } else {
            valuesDir = new File(valuesLocation);
        }

        if (valuesDir != null && valuesDir.exists()) {
            Path stringsPath = Paths.get(valuesDir.getCanonicalPath(),
                    generateAnnotation.filePrefix() + interfaze.getSimpleName() + ".xml");
            if (Files.exists(stringsPath)) {
                List<String> lines = Files.readAllLines(stringsPath, StandardCharsets.UTF_8);
                if (lines != null && !lines.isEmpty()) {
                    if (STRING_RESOURCES_HINT.startsWith(lines.get(0))) {
                        Files.deleteIfExists(stringsPath);
                    } else {
                        warner.emitError("File '" + stringsPath.toString() + "' was not generated by esperandro and " +
                                "will therefore not get deleted.", interfaze);
                    }
                } else {
                    Files.deleteIfExists(stringsPath);
                }
            }

            final StringBuilder resFile = new StringBuilder(STRING_RESOURCES_HINT);
            resFile.append("<resources>\n");
            for (PreferenceInformation info : allPreferences) {
                resFile.append("    <string translatable=\"false\" name=\"")
                        .append(generateAnnotation.stringPrefix())
                        .append(info.preferenceName)
                        .append("\">")
                        .append(info.preferenceName)
                        .append("</string>\n");
            }
            resFile.append("</resources>");

            Files.write(stringsPath, resFile.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            warner.emitError("The resource directory could not be found automatically." +
                    " Please provide the option '" + EsperandroAnnotationProcessor.OPTION_VALUES_DIR + "' in your build.gradle to point it " +
                    "to the res/values directory.", interfaze);
        }
    }

    // https://stackoverflow.com/a/37230331
    private File findValues() {
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