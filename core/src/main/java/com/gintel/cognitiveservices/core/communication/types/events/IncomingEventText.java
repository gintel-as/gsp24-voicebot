package com.gintel.cognitiveservices.core.communication.types.events;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;

public class IncomingEventText extends BaseEvent {
    private final String sessionId;
    private final EventHandler<BaseEvent> eventHandler;
    private final String text;
    private final String translationService;
    private final String tranlsationLanguage;
    private final String ttsService;
    private final String sttService;
    private final String aiService;
    private final MediaStream outputStream;

    public IncomingEventText(String sessionId,
            EventHandler<BaseEvent> eventHandler,
            MediaStream outputStream,
            String text,
            String translationService,
            String tranlsationLanguage,
            String ttsService,
            String sttService,
            String aiService) {
        this.outputStream = outputStream;
        this.sessionId = sessionId;
        this.eventHandler = eventHandler;
        this.text = text;
        this.translationService = "";
        this.tranlsationLanguage = "";
        this.ttsService = "";
        this.sttService = "";
        this.aiService = "";
    }

    public String getSessionId() {
        return sessionId;
    }

    public EventHandler<BaseEvent> getEventHandler() {
        return eventHandler;
    }

    @Override
    public String toString() {
        return text;
    }

    public String getText() {
        return text;
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
