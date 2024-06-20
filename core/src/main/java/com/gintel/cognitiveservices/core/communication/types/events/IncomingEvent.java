package com.gintel.cognitiveservices.core.communication.types.events;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.stt.EventHandler;

public class IncomingEvent extends BaseEvent {
    private final String sessionId;
    private final EventHandler<BaseEvent> eventHandler;

    public IncomingEvent(String sessionId, EventHandler<BaseEvent> eventHandler) {
        this.sessionId = sessionId;
        this.eventHandler = eventHandler;
    }

    public String getSessionId() {
        return sessionId;
    }

    public EventHandler<BaseEvent> getEventHandler() {
        return eventHandler;
    }
}