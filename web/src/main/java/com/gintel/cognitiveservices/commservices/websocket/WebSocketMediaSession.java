package com.gintel.cognitiveservices.commservices.websocket;

import javax.websocket.Session;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;


public class WebSocketMediaSession extends MediaSession {
    private Session wsSession;

    public WebSocketMediaSession(String id, EventHandler<BaseEvent> callback, MediaStream inputStream, Session wsSession) {
        super(id, callback, inputStream);
        this.wsSession = wsSession;
    }
    
    public Session getWsSession() {
        return wsSession;
    }
}