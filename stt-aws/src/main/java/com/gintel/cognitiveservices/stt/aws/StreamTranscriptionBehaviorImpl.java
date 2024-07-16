package com.gintel.cognitiveservices.stt.aws;

import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionResponse;
import software.amazon.awssdk.services.transcribestreaming.model.TranscriptEvent;
import software.amazon.awssdk.services.transcribestreaming.model.TranscriptResultStream;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.stt.SpeechToTextEvent;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextStatus;

public class StreamTranscriptionBehaviorImpl implements StreamTranscriptionBehavior {

    private final EventHandler<BaseEvent> eventHandler;

    public StreamTranscriptionBehaviorImpl(EventHandler<BaseEvent> eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void onError(Throwable e) {
        eventHandler.onEvent(this, new SpeechToTextEvent("Error: " + e.getMessage(), SpeechToTextStatus.ERROR));
    }

    @Override
    public void onStream(TranscriptResultStream e) {
        ((TranscriptEvent) e).transcript().results().forEach(result -> {
            result.alternatives().forEach(alternative -> {
                eventHandler.onEvent(this, new SpeechToTextEvent("RECOGNIZED: " + alternative.transcript(),
                        SpeechToTextStatus.RECOGNIZED));
            });
        });
    }

    @Override
    public void onResponse(StartStreamTranscriptionResponse r) {
        eventHandler.onEvent(this, new SpeechToTextEvent("Session started event.", SpeechToTextStatus.STARTED));
    }

    @Override
    public void onComplete() {
        eventHandler.onEvent(this, new SpeechToTextEvent("Session stopped event.", SpeechToTextStatus.STOPPED));
    }
}