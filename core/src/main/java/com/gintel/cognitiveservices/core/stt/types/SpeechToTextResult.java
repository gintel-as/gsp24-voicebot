package com.gintel.cognitiveservices.core.stt.types;



public class SpeechToTextResult {
    private SpeechToTextStatus status;
    private byte[] text;
    private String srt;

    public SpeechToTextResult() {
    }

    public SpeechToTextResult(SpeechToTextStatus status, byte[] text, String srt) {
        this.status = status;
        this.text = text;
        this.srt = srt;
    }

    public byte[] getText() {
        return text;
    }

    public void setText(byte[] text) {
        this.text = text;
    }

    public SpeechToTextStatus getStatus() {
        return status;
    }

    public void setStatus(SpeechToTextStatus status) {
        this.status = status;
    }

    public String getSrt() {
        return srt;
    }

    public void setSrt(String srt) {
        this.srt = srt;
    }
}
