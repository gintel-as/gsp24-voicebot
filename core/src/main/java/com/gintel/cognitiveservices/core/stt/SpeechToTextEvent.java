package com.gintel.cognitiveservices.core.stt;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;

public class SpeechToTextEvent extends BaseEvent {
    private String data;

    public SpeechToTextEvent (String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }
}
