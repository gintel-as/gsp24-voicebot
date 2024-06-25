package com.gintel.cognitiveservices.core.translation;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;

public class TranslationEvent extends BaseEvent {
    private String data;

    public TranslationEvent (String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

}