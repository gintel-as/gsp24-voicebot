package com.gintel.cognitiveservices.core.communication.types.events;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;

public class IncomingEventText extends BaseEvent {
    private final String sessionId;
    private final EventHandler<BaseEvent> eventHandler;
    private final String text;

    public IncomingEventText(String sessionId, EventHandler<BaseEvent> eventHandler, String text) {
        this.sessionId = sessionId;
        this.eventHandler = eventHandler;
        this.text = text;
    }

    public String getSessionId() {
        return sessionId;
    }

    public EventHandler<BaseEvent> getEventHandler() {
        return eventHandler;
    }
    @Override
    public String toString(){
        return text;
    }

    public String getText() {
        return text;
    }
}
    

