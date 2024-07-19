package com.gintel.cognitiveservices.core.tts.types;

public class TextToSpeechResult {
    private TextToSpeechStatus status;
    private byte[] audio;
    private String srt;

    public TextToSpeechResult() {
    }

    public TextToSpeechResult(TextToSpeechStatus status, byte[] audio, String srt) {
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

    public TextToSpeechStatus getStatus() {
        return status;
    }

    public void setStatus(TextToSpeechStatus status) {
        this.status = status;
    }

    public String getSrt() {
        return srt;
    }

    public void setSrt(String srt) {
        this.srt = srt;
    }
}
