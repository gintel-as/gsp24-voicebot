package com.gintel.cognitiveservices.core.communication.types.events;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;

public class IncomingEvent extends BaseEvent {
    private final String sessionId;
    private final EventHandler<BaseEvent> eventHandler;
    private final MediaStream outputStream;
    private final String translationService;
    private final String tranlsationLanguage;
    private final String ttsService;
    private final String sttService;
    private final String aiService;

    public IncomingEvent(String sessionId,
            EventHandler<BaseEvent> eventHandler,
            MediaStream outputStream,
            String translationService,
            String tranlsationLanguage,
            String ttsService,
            String sttService,
            String aiService) {
        this.sessionId = sessionId;
        this.eventHandler = eventHandler;
        this.outputStream = outputStream;
        this.tranlsationLanguage = tranlsationLanguage;
        this.translationService = translationService;
        this.ttsService = ttsService;
        this.sttService = sttService;
        this.aiService = aiService;
    }

    public String getSessionId() {
        return sessionId;
    }

    public EventHandler<BaseEvent> getEventHandler() {
        return eventHandler;
    }

    public MediaStream getOutputStream() {
        return outputStream;
    }

    public String getTranslationService() {
        return translationService;
    }
    
    public String getAiService() {
        return aiService;
    }

    public String getTtsService() {
        return ttsService;
    }

    public String getSttService() {
        return sttService;
    }

    public String getTranlsationLanguage() {
        return tranlsationLanguage;
    }
}
