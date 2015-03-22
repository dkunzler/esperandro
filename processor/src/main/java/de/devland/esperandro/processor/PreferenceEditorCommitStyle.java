package de.devland.esperandro.processor;

import java.io.IOException;

import com.squareup.javawriter.JavaWriter;

public enum PreferenceEditorCommitStyle {
    COMMIT("commit()"), APPLY("apply()");

    private String statementPart;


    private PreferenceEditorCommitStyle(String statementPart) {
        this.statementPart = statementPart;
    }

    public String getStatementPart() {
        return statementPart;
    }

    public static void emitPreferenceCommitAction(JavaWriter writer,
                                                  PreferenceEditorCommitStyle commitStyle,
                                                  StringBuilder statementPattern) throws IOException {

        String statement = String.format(statementPattern.toString(), commitStyle.getStatementPart());
        writer.emitStatement(statement);
    }
}
