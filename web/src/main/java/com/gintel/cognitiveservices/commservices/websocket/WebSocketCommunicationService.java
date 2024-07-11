package com.gintel.cognitiveservices.commservices.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
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
import com.gintel.cognitiveservices.core.communication.types.events.IncomingEvent;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.service.CognitiveServices;

@ServerEndpoint(value = "/websocket")
public class WebSocketCommunicationService implements CommunicationService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketCommunicationService.class);

    private List<CommunicationServiceListener> listeners = new ArrayList<>();

    private Map<String, MediaSession> sessions = new ConcurrentHashMap<>();
    private Map<String, Session> wsSessions = new ConcurrentHashMap<>();
    private Map<String, ChatBotContext> contexts = new ConcurrentHashMap<>();

    // change to false to test async tts
    private boolean synchronousTts = true;

    public WebSocketCommunicationService() {
        logger.info("Created");

        addListener(CognitiveServices.getInstance());
    }

    @OnMessage
    public void onVoiceInput(Session session, String msg, boolean last) {
        logger.trace("onVoiceInput: {}", msg);

        try {
            if (!sessions.containsKey(session.getId())) {
                logger.warn("Session not found");
                session.getBasicRemote().sendText("Session " + session.getId() + " not found");
                return;
            }

            if (msg.contains("Language:")) {
                contexts.get(session.getId()).setLanguage(msg.replace("Language:", ""));
            } else {
                byte[] bytes = Base64.getDecoder().decode(msg);
                sessions.get(session.getId()).getInputStream().write(bytes);
            }
        } catch (Exception ex) {
            logger.error("Exception in onVoiceInput", ex);

            try {
                session.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @OnMessage
    public void onVoiceInput(Session session, ByteBuffer bb, boolean last) {
        logger.trace("onVoiceInputBinary");

        try {
            session.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        try {
            String sessionId = session.getId();

            logger.info("onOpen({}, {})", sessionId, config);

            wsSessions.put(sessionId, session);
            contexts.put(sessionId, new ChatBotContext());

            EventHandler<BaseEvent> handler = (s, e) -> {
                try {
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
                    public void write(byte[] data) {
                        logger.info("write(data={} bytes)", data.length);

                        try {
                            wsSessions.get(sessionId).getBasicRemote().sendBinary(ByteBuffer.wrap(data));
                        } catch (Exception e) {
                            logger.error("Exception writing output mediaStream for session {}",
                                    session.getId(), e);
                        }
                    }

                    @Override
                    public void close() {

                    }
                };
            }

            String translationService = session.getPathParameters().get("translationService");
            String translationLanguage = session.getPathParameters().get("translationLanguage");
            String ttsService = session.getPathParameters().get("ttsService");
            String sttService = session.getPathParameters().get("sttService");
            String aiService = session.getPathParameters().get("aiService");

            listeners.forEach(
                    c -> c.onEvent(this, new IncomingEvent(session.getId(), handler, outputStream,
                            translationService,
                            translationLanguage,
                            ttsService,
                            sttService,
                            aiService),
                            contexts.get(sessionId)));
        } catch (Exception ex) {
            logger.error("Exception in onOpen", ex);
        }
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
        System.out.println("echoPong");
    }

    @Override
    public void playMedia(String sessionId, String data) {
        try {
            wsSessions.get(sessionId).getBasicRemote().sendText(new String(data));
        } catch (IOException e) {
            logger.error("Exception in playmedia", e);
        }
    }

    @Override
    public void playMedia(String sessionId, byte[] data) {
        try {
            wsSessions.get(sessionId).getBasicRemote().sendBinary(ByteBuffer.wrap(data));
        } catch (IOException e) {
            logger.error("Exception in playmedia", e);
        }
    }

    @Override
    public void answer(MediaSession mediaSession) {
        logger.info("answer(session={})", mediaSession);

        // if (!(mediaSession instanceof WebSocketMediaSession)) {
        // logger.warn("answer({}): Unsupported session type", mediaSession);
        // return;
        // }
        sessions.put(mediaSession.getId(), mediaSession);
    }

    @Override
    public void reject() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void addListener(CommunicationServiceListener listener) {
        listeners.add(listener);
    }

    @Override
    public String toString() {
        return getServiceName();
    }
}
