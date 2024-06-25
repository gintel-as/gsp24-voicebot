package com.gintel.cognitiveservices.core.translation.types;

public class TranslationResult {
    private TranslationStatus status;
    private String srt;

    public TranslationResult() {
    }

    public TranslationResult(TranslationStatus status, String srt) {
        this.status = status;
        this.srt = srt;
    }

    public TranslationStatus getStatus() {
        return status;
    }

    public void setStatus(TranslationStatus status) {
        this.status = status;
    }

    public String getSrt() {
        return srt;
    }

    public void setSrt(String srt) {
        this.srt = srt;
    }
}
