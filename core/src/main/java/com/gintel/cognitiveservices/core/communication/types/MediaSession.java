package com.gintel.cognitiveservices.core.communication.types;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;

public class MediaSession {
    private final String id;
    private final EventHandler<BaseEvent> callback;
    private final MediaStream inputStream;
  

    public MediaSession(String id, EventHandler<BaseEvent> callback, MediaStream inputStream) {
        this.id = id;
        this.callback = callback;
        this.inputStream = inputStream;
        
    }

    public EventHandler<BaseEvent> getCallback() {
        return callback;
    }

    public String getId() {
        return id;
    }

    public MediaStream getInputStream() {
        return inputStream;
    }

    public TextToSpeechResult getTextInput(TextToSpeechResult textToSpeech) {
        return textToSpeech;
     
    }
}
