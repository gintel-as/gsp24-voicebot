package com.gintel.cognitiveservices.core.translation.types;

public class TranslationResult {
    private TranslationStatus status;
    private String output;

    public TranslationResult() {
    }

    public TranslationResult(TranslationStatus status, String output) {
        this.status = status;
        this.output = output;
    }

    public TranslationStatus getStatus() {
        return status;
    }

    public void setStatus(TranslationStatus status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
