package com.gintel.cognitiveservices.core.tts;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;

public class TextToSpeechEvent extends BaseEvent {
    private String data;

    public TextToSpeechEvent (String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }
}