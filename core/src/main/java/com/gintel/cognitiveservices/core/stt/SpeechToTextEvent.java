package com.gintel.cognitiveservices.core.stt;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextStatus;

public class SpeechToTextEvent extends BaseEvent {
    private final String data;
    private final SpeechToTextStatus result;

    public SpeechToTextEvent (String data, SpeechToTextStatus result) {
        this.data = data;
        this.result = result;
    }

    @Override
    public String toString() {
        return data;
    }

    public String getData() {
        return data;
    }

    public SpeechToTextStatus getResult() {
        return result;
    }
}
