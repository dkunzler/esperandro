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

    public static void emitPreferenceCommitActionWithVersionCheck(JavaWriter writer,
                                                                  PreferenceEditorCommitStyle commitStyle,
                                                                  StringBuilder statementPattern) throws IOException {
        if (commitStyle.equals(PreferenceEditorCommitStyle.APPLY)) {
            writer.beginControlFlow("if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)");
            String statement = String.format(statementPattern.toString(), PreferenceEditorCommitStyle.COMMIT
                    .getStatementPart());
            writer.emitStatement(statement);
            writer.nextControlFlow("else");
        }
        String statement = String.format(statementPattern.toString(), commitStyle.getStatementPart());
        writer.emitStatement(statement);
        if (commitStyle.equals(PreferenceEditorCommitStyle.APPLY)) {
            writer.endControlFlow();
        }
    }
}
