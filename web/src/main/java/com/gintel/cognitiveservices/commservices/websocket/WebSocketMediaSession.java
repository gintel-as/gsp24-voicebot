package com.gintel.cognitiveservices.commservices.websocket;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.stt.EventHandler;
import com.gintel.cognitiveservices.core.stt.types.MediaStream;
import javax.websocket.Session;

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
