package com.gintel.cognitiveservices.core.translation.types;

public class TranslationResult {
    private TranslationStatus status;
    private byte[] audio;
    private String srt;

    public TranslationResult() {
    }

    public TranslationResult(TranslationStatus status, byte[] audio, String srt) {
        this.status = status;
        this.audio = audio;
        this.srt = srt;
    }

    public byte[] getAudio() {
        return audio;
    }

    public void setAudio(byte[] audio) {
        this.audio = audio;
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
