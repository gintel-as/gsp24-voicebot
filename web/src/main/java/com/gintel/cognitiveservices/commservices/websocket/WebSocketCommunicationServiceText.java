package com.gintel.cognitiveservices.commservices.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.communication.CommunicationService;
import com.gintel.cognitiveservices.core.communication.CommunicationServiceListener;
import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.communication.types.events.IncomingEventText;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.service.CognitiveServices;

@ServerEndpoint(value = "/websocket/text")
public class WebSocketCommunicationServiceText implements CommunicationService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketCommunicationServiceText.class);

    // private AzureTextToSpeechService ttsService;
    private List<CommunicationServiceListener> listeners = new ArrayList<>();
    private Map<String, MediaSession> sessions = new ConcurrentHashMap<>();
    private Map<String, Session> wsSessions = new ConcurrentHashMap<>();
    private Map<String, ChatBotContext> contexts = new ConcurrentHashMap<>();
    private boolean synchronousTts = true;

    public WebSocketCommunicationServiceText() {
        logger.info("Created");
        addListener(CognitiveServices.getInstance());
    }

    @OnMessage
    public void onTextInput(Session session, String msg, boolean last) {
        String sessionId = session.getId();

        logger.info("onTextInput: {}", msg);
        try {
            if (!sessions.containsKey(session.getId())) {
                logger.warn("Session {} not found in sessions map. Current sessions: {}", session.getId(),
                        sessions.keySet());
                session.getBasicRemote().sendText("Session " + session.getId() + " not found");
                return;
            }
           if (msg.contains("newTTS:")) {
                contexts.get(session.getId()).setChosenTts(msg.replace("newTTS:", ""));
            } else if (msg.contains("newAI:")) {
                contexts.get(session.getId()).setChosenAi(msg.replace("newAI:", ""));
            }

            EventHandler<BaseEvent> handler = (s, e) -> {
                try {
                    logger.info(e.toString() + "hello");
                    session.getBasicRemote().sendText(e.toString());
                } catch (IOException ex) {
                    logger.error("Exception when sending text to client", ex);
                }
            };
            MediaStream outputStream;
            if (synchronousTts) {
                outputStream = null;
            } else {
                outputStream = new MediaStream() {
                    @Override
                    public void write(String data) {
                        logger.info("write(data={} bytes)", data.length());

                        try {
                            wsSessions.get(sessionId).getBasicRemote().sendText(new String(data));
                        } catch (Exception e) {
                            logger.error("Exception writing output mediaStream for session {}",
                                    session.getId(), e);
                        }
                    }

                    @Override
                    public void write(byte[] data) {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'write'");
                    }

                    @Override
                    public void close() {
                        // TODO Auto-generated method stub
                        throw new UnsupportedOperationException("Unimplemented method 'close'");
                    }
                };
            }

            // TextToSpeechResult ttsResult = ttsService.textToSpeech(lang,
            // "en-US-AvaMultilingualNeural", decodedMsg, null, null);
            // sessions.get(sessionId).getTextInput(ttsResult);
            String translationService = session.getPathParameters().get("translationService");
            String translationLanguage = session.getPathParameters().get("translationLanguage");
            String ttsService = session.getPathParameters().get("ttsService");
            String sttService = session.getPathParameters().get("sttService");
            String aiService = session.getPathParameters().get("aiService");

            for (CommunicationServiceListener listener : listeners) {
                listener.onEvent(
                        this, new IncomingEventText(sessionId, handler, outputStream, msg, translationService,
                                translationLanguage, ttsService, sttService, aiService),
                        contexts.get(sessionId));

            }

            // String lang = "nb-NO";

        } catch (Exception e) {
            logger.error("Exception in onTextInput", e);
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String sessionId = session.getId();
        logger.info("onOpen({}, {})", sessionId, config);
        wsSessions.put(session.getId(), session);
        contexts.put(sessionId, new ChatBotContext());
        logger.info(contexts.toString() + "this is context");
        MediaStream outputStream;
        if (synchronousTts) {
            outputStream = null;
        } else {
            outputStream = new MediaStream() {
                @Override
                public void write(String data) {
                    logger.info("write(data={} bytes)", data.length());

                    try {
                        wsSessions.get(sessionId).getBasicRemote().sendText(new String(data));
                    } catch (Exception e) {
                        logger.error("Exception writing output mediaStream for session {}",
                                session.getId(), e);
                    }
                }

                @Override
                public void close() {

                }

                @Override
                public void write(byte[] data) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'write'");
                }
            };
        }

        String translationService = session.getPathParameters().get("translationService");
        String translationLanguage = session.getPathParameters().get("translationLanguage");
        String ttsService = session.getPathParameters().get("ttsService");
        String sttService = session.getPathParameters().get("sttService");
        String aiService = session.getPathParameters().get("aiService");

        EventHandler<BaseEvent> handler = (s, e) -> {
            try {
                logger.info("Sending event data to client: {}", e);
                session.getBasicRemote().sendText(e.toString());
            } catch (IOException ex) {
                logger.error("Exception when sending text to client", ex);
            }
        };

        listeners.forEach(
                c -> c.onEvent(this,
                        new IncomingEventText(sessionId, handler, outputStream, null,
                                translationService,
                                translationLanguage,
                                ttsService,
                                sttService,
                                aiService),
                        contexts.get(sessionId)));
    }

    @OnClose
    public void onClose(Session session) {
        logger.info("onClose({})", session.getId());

        MediaSession localSession = sessions.remove(session.getId());
        if (localSession != null) {
            localSession.getInputStream().close();
        }
        wsSessions.remove(session.getId());
        contexts.remove(session.getId());
    }

    @OnMessage
    public void echoPongMessage(PongMessage pm) {
        logger.info("echoPongMessage: {}", pm);
    }

    @Override
    public void playMedia(String sessionId, String data) {
        try {
            wsSessions.get(sessionId).getBasicRemote().sendText(new String(data));
        } catch (Exception e) {
            logger.error("Exception in playMedia with sessionId {}: {}", sessionId, e);
        }
    }

    @Override
    public void reject() {
        logger.info("reject called");
    }

    @Override
    public void disconnect() {
        logger.info("disconnect called");
    }

    @Override
    public void addListener(CommunicationServiceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void answer(MediaSession mediaSession) {
        // String sessionId = mediaSession.getId();
        logger.info("answer(session={})", mediaSession);
        sessions.put(mediaSession.getId(), mediaSession);
        logger.info("Current sessions after adding: {}", sessions.keySet());
    }

    @Override
    public String toString() {
        return getServiceName();
    }

    @Override
    public void playMedia(String sessionId, byte[] data) {
        try {
            wsSessions.get(sessionId).getBasicRemote().sendBinary(ByteBuffer.wrap(data));
        } catch (IOException e) {
            logger.error("Exception in playMedia with sessionId {}: {}", sessionId, e);
        }
    }

}