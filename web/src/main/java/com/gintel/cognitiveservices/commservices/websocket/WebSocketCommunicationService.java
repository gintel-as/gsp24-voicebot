package com.gintel.cognitiveservices.commservices.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
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

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.communication.CommunicationService;
import com.gintel.cognitiveservices.core.communication.CommunicationServiceListener;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.communication.types.events.IncomingEvent;
import com.gintel.cognitiveservices.core.stt.EventHandler;
import com.gintel.cognitiveservices.openai.azure.AzureOpenaiConfig;
import com.gintel.cognitiveservices.openai.azure.AzureOpenaiService;
import com.gintel.cognitiveservices.service.CognitiveServices;
import com.gintel.cognitiveservices.service.Service;
import com.gintel.cognitiveservices.stt.azure.AzureSTTConfig;
import com.gintel.cognitiveservices.stt.azure.AzureSpeechToTextService;

@ServerEndpoint(value = "/websocket")
public class WebSocketCommunicationService implements CommunicationService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketCommunicationService.class);

    private List<CommunicationServiceListener> listeners = new ArrayList<>();

    private Map<String, MediaSession> sessions = new ConcurrentHashMap<>();
    private Map<String, Session> wsSessions = new ConcurrentHashMap<>();

    public WebSocketCommunicationService() {
        logger.info("Created");

        String strPath = System.getProperty("catalina.base");
        ConfigFactory.setProperty("config_file", strPath + "/conf/web.properties");
        Map<String, Service> services = new HashMap<>();
        services.put("ws", this);
        services.put("azure-stt", new AzureSpeechToTextService(ConfigFactory.create(AzureSTTConfig.class)));
        new CognitiveServices(services);
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

            byte[] bytes = Base64.getDecoder().decode(msg);
            sessions.get(session.getId()).getInputStream().write(bytes);
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
        String sessionId = session.getId();

        logger.info("onOpen({}, {})", sessionId, config);

        wsSessions.put(session.getId(), session);

        EventHandler<BaseEvent> handler = (s, e) -> {
            try {
                session.getBasicRemote().sendText(e.toString());
            } catch (IOException ex) {
                logger.error("Exception when sending text to client", ex);
            }    
        };

        listeners.forEach(c -> c.onEvent(this, new IncomingEvent(session.getId(), handler)));
    }

    @OnClose
    public void onClose(Session session) {
        logger.info("onClose({})", session.getId());

        MediaSession localSession = sessions.remove(session.getId());
        if (localSession != null) {
            localSession.getInputStream().close();
        }
        wsSessions.remove(session.getId());
    }

    @OnMessage
    public void echoPongMessage(PongMessage pm) {
        System.out.println("echoPong");
    }

    @Override
    public void playMedia(String sessionId, byte[] data) {
        try {
            wsSessions.get(sessionId).getBasicRemote().sendText(new String(data));
        } catch (IOException e) {
            logger.error("Exception in playmedia", e);
        }
    }

    @Override
    public void answer(MediaSession mediaSession) {
        logger.info("answer(session={})", mediaSession);

//        if (!(mediaSession instanceof WebSocketMediaSession)) {
//            logger.warn("answer({}): Unsupported session type", mediaSession);
//            return;
//        }
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
