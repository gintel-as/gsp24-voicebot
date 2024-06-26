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
import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.communication.types.events.IncomingEvent;
import com.gintel.cognitiveservices.core.communication.types.events.IncomingEventText;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.openai.azure.AzureOpenaiConfig;
import com.gintel.cognitiveservices.openai.azure.AzureOpenaiService;
import com.gintel.cognitiveservices.service.CognitiveServices;
import com.gintel.cognitiveservices.service.Service;
import com.gintel.cognitiveservices.stt.azure.AzureSTTConfig;
import com.gintel.cognitiveservices.stt.azure.AzureSpeechToTextService;
import com.gintel.cognitiveservices.tts.azure.AzureTTSConfig;
import com.gintel.cognitiveservices.tts.azure.AzureTextToSpeechService;

@ServerEndpoint(value = "/websocket/text")
public class WebSocketCommunicationServiceText implements CommunicationService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketCommunicationServiceText.class);

    private AzureTextToSpeechService ttsService;
    private List<CommunicationServiceListener> listeners = new ArrayList<>();
    private Map<String, MediaSession> sessions = new ConcurrentHashMap<>();
    private Map<String, Session> wsSessions = new ConcurrentHashMap<>();
    private Map<String, ChatBotContext> contexts = new ConcurrentHashMap<>();

    public WebSocketCommunicationServiceText() {
        logger.info("Created");
       String strPath = System.getProperty("catalina.base");
        ConfigFactory.setProperty("config_file", strPath + "/conf/web.properties");
        Map<String, Service> services = new HashMap<>();
        services.put("ws", this);
        services.put("azure-stt", new AzureSpeechToTextService(ConfigFactory.create(AzureSTTConfig.class)));
        services.put("azure-openai", new AzureOpenaiService(ConfigFactory.create(AzureOpenaiConfig.class)));
        services.put("azure-tts", new AzureTextToSpeechService(ConfigFactory.create(AzureTTSConfig.class)));
        new CognitiveServices(services);
        this.ttsService = new AzureTextToSpeechService(ConfigFactory.create(AzureTTSConfig.class));
    }

    @OnMessage
    public void onTextInput(Session session, String msg, boolean last) {
        String sessionId = session.getId();
        logger.info("onTextInput: {}", msg);
        try {
            if (!sessions.containsKey(session.getId())) {
                logger.warn("Session {} not found in sessions map. Current sessions: {}", session.getId(), sessions.keySet());
                session.getBasicRemote().sendText("Session " + session.getId() + " not found");
                return;
            }

            String lang = "nb-NO";
            
            EventHandler<BaseEvent> handler = (s, e) -> {
                try {
                    logger.info(e.toString() + "hello");
                    session.getBasicRemote().sendText(e.toString());
                } catch (IOException ex) {
                    logger.error("Exception when sending text to client", ex);
                }
            };
            
            //TextToSpeechResult ttsResult = ttsService.textToSpeech(lang, "en-US-AvaMultilingualNeural", decodedMsg, null, null);
           // sessions.get(sessionId).getTextInput(ttsResult);
            
            for (CommunicationServiceListener listener : listeners) {
                listener.onEvent(this, new IncomingEventText(sessionId, handler, msg), contexts.get(sessionId));
            }
            

        } catch (Exception e) {
            logger.error("Exception in onTextInput", e);
        }
    }
    private boolean isBase64Encoded(String message) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(message);
            String reEncodedMessage = Base64.getEncoder().encodeToString(decodedBytes);
            return message.equals(reEncodedMessage);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String sessionId = session.getId();
        logger.info("onOpen({}, {})", sessionId, config);
        wsSessions.put(session.getId(), session);
        contexts.put(sessionId, new ChatBotContext());
        
       

        EventHandler<BaseEvent> handler = (s, e) -> { //the problem is here!
            try {
                logger.info("Sending event data to client: {}", e);
                session.getBasicRemote().sendText(e.toString());
            } catch (IOException ex) {
                logger.error("Exception when sending text to client", ex);
            }
        };

        listeners.forEach(c -> c.onEvent(this, new IncomingEventText(sessionId, handler,null), contexts.get(sessionId)));
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
        String sessionId = mediaSession.getId();
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
