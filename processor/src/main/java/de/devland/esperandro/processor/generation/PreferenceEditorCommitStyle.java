package de.devland.esperandro.processor.generation;

public enum PreferenceEditorCommitStyle {
    COMMIT("commit()"), APPLY("apply()");

    private String statementPart;

    PreferenceEditorCommitStyle(String statementPart) {
        this.statementPart = statementPart;
    }

    public String getStatementPart() {
        return statementPart;
    }

}
