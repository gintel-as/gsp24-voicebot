package com.gintel.cognitiveservices.core.communication;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;

public interface CommunicationServiceListener {
    void onEvent(CommunicationService service, BaseEvent event);
}
