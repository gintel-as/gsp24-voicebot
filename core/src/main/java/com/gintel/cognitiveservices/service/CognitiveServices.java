package com.gintel.cognitiveservices.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.communication.CommunicationService;
import com.gintel.cognitiveservices.core.communication.CommunicationServiceListener;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.communication.types.events.AnsweredEvent;
import com.gintel.cognitiveservices.core.communication.types.events.IncomingEvent;
import com.gintel.cognitiveservices.core.openai.Openai;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.core.openai.types.OpenaiResult;
import com.gintel.cognitiveservices.core.stt.EventHandler;
import com.gintel.cognitiveservices.core.stt.SpeechToText;
import com.gintel.cognitiveservices.core.stt.SpeechToTextEvent;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;

public class CognitiveServices implements CommunicationServiceListener {
    private static final Logger logger = LoggerFactory.getLogger(CognitiveServices.class);

    private Map<String, Service> services;

    public CognitiveServices(Map<String, Service> services) {
        this.services = services;

        getServices(CommunicationService.class).forEach(c -> c.addListener(this));
    }

    @Override
    public void onEvent(CommunicationService service, BaseEvent event) {
        logger.info("onEvent(service={}, event={})", service, event);

        if (event instanceof IncomingEvent) {
            handleIncoming(service, (IncomingEvent) event);
        } else if (event instanceof AnsweredEvent) {
            // service.playMedia();
        }
    }

    private void handleIncoming(CommunicationService service, IncomingEvent event) {
        EventHandler<BaseEvent> handler = (s, e) -> {
            try {
//                session.getBasicRemote().sendText(e.toString());
                if (e instanceof SpeechToTextEvent) {
                    SpeechToTextEvent casted = (SpeechToTextEvent) e;
                    if (e.toString().charAt(9) == 'D') {
                        // todo openai integration
                        for (Openai ai : getServices(Openai.class)){
                            service.playMedia(event.getSessionId(), e.toString());
                            ChatBotContext ctx = new ChatBotContext();
                            OpenaiResult aiResult = ai.openai(e.toString().replaceAll("RECOGNIZED: ", ""), ctx, null, null);
                            // TextToSpeech tts = (TextToSpeech) getServices(TextToSpeech.class);
                            // TextToSpeechResult ttsResult = tts.textToSpeech("en-US", "en-US-AvaMultilingualNeural", aiResult.getResponse(), null, null);
                            service.playMedia(event.getSessionId(), aiResult.getResponse());
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Exception when sending text to client", ex);
            }    
        };

        for (SpeechToText stt : getServices(SpeechToText.class)) {
            MediaSession session = stt.startSpeechToTextSession(event.getSessionId(), null,
                    handler);
            service.answer(session);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Service> List<T> getServices(Class<T> clazz) {
        return services.values().stream()
                .filter(clazz::isInstance)
                .map(c -> (T) c)
                .collect(Collectors.toList());
    }
}
