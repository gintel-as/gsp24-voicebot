package com.gintel.cognitiveservices.core.tts;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechStatus;

public class TextToSpeechEvent extends BaseEvent {
    private String data;
    private final TextToSpeechStatus result;

    public TextToSpeechEvent(String data) {
        this.result = null;
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

    public TextToSpeechStatus getResult() {
        return result;
    }
}