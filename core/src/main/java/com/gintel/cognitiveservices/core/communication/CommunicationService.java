package com.gintel.cognitiveservices.core.communication;

import com.gintel.cognitiveservices.core.communication.types.MediaSession;

import com.gintel.cognitiveservices.service.Service;

public interface CommunicationService extends Service {
    void playMedia(String sessionId, String data);
    
    void playMedia(String sessionId, byte[] data);

    void answer(MediaSession session);
   
    void reject();

    void disconnect();

    void addListener(CommunicationServiceListener listener);
}
