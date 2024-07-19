package com.gintel.cognitiveservices.core.communication;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;

public interface CommunicationServiceListener {
    void onEvent(CommunicationService service, BaseEvent event, ChatBotContext ctx);
}
