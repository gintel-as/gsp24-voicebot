package com.gintel.cognitiveservices.core.stt.types;



public class SpeechToTextResult {
    private SpeechToTextStatus status;
    private String text;
    private String detectedLanguage;

    public SpeechToTextResult() {
    }

    public SpeechToTextResult(SpeechToTextStatus status, String text, String detectedLanguage) {
        this.status = status;
        this.text = text;
        this.detectedLanguage = detectedLanguage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SpeechToTextStatus getStatus() {
        return status;
    }

    public void setStatus(SpeechToTextStatus status) {
        this.status = status;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setDetectedLanguage(String detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }
}
