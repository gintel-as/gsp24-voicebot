package com.gintel.cognitiveservices.core.tts.types;

public class TextToSpeechByteResult {
    private TextToSpeechStatus status;
    private byte[] audio;

    public TextToSpeechByteResult() {
    }

    public TextToSpeechByteResult(TextToSpeechStatus status, byte[] audio) {
        this.status = status;
        this.audio = audio;
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
}
