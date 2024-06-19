package com.gintel.cognitiveservices.service;

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
import com.gintel.cognitiveservices.core.stt.SpeechToText;

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
            service.playMedia();
        }
    }

    private void handleIncoming(CommunicationService service, IncomingEvent event) {
        for (SpeechToText stt : getServices(SpeechToText.class)) {
            MediaSession session = stt.startSpeechToTextSession(event.getSessionId(), null,
                    event.getEventHandler());
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
