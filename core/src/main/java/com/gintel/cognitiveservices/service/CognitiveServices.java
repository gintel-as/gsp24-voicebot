package com.gintel.cognitiveservices.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.communication.CommunicationService;
import com.gintel.cognitiveservices.core.communication.CommunicationServiceListener;
import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.communication.types.events.AnsweredEvent;
import com.gintel.cognitiveservices.core.communication.types.events.IncomingEvent;
import com.gintel.cognitiveservices.core.communication.types.events.IncomingEventText;
import com.gintel.cognitiveservices.core.openai.Openai;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.core.openai.types.OpenaiResult;
import com.gintel.cognitiveservices.core.stt.SpeechToText;
import com.gintel.cognitiveservices.core.stt.SpeechToTextEvent;
import com.gintel.cognitiveservices.core.translation.Translation;
import com.gintel.cognitiveservices.core.translation.types.TranslationResult;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextStatus;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.TextToSpeechEvent;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechByteResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechStatus;

public class CognitiveServices implements CommunicationServiceListener {
    private static final Logger logger = LoggerFactory.getLogger(CognitiveServices.class);

    private Map<String, String> ttsVoices = new ConcurrentHashMap<>();
    private Map<String, String> ttsLanguages = new ConcurrentHashMap<>();

    private List<String> sttProviders = Arrays.asList("azure", "google");
    private List<String> ttsProviders = Arrays.asList("azure", "google");
    private int sttChosenProvider = 0;
    private int ttsChosenProvider = 0;
    // 0 = Azure
    // 1 = Google

    private static CognitiveServices instance;
    private Map<String, Service> services;

    private CognitiveServices(List<Service> services) {
        this.services = services.stream()
                .collect(Collectors.toMap(Service::getServiceName, s -> s));

        ttsVoices.put("azure", "en-US-AvaMultilingualNeural");
        ttsVoices.put("google", "en-US-Standard-A");

        ttsLanguages.put("none", "en-US-Standard-A");
        ttsLanguages.put("en-US", "en-US-Standard-A");
        ttsLanguages.put("nb-NO", "nb-NO-Standard-A");
        ttsLanguages.put("sv-SE", "sv-SE-Standard-A");
        ttsLanguages.put("da-DK", "da-DK-Standard-A");
        ttsLanguages.put("fa-IR", "fa-IR-Standard-A");
    }

    public static synchronized void init(List<Service> services) {
        instance = new CognitiveServices(services);
    }

    public static CognitiveServices getInstance() {
        return instance;
    }

    @Override
    public void onEvent(CommunicationService service, BaseEvent event, ChatBotContext ctx) {
        logger.info("onEvent(service={}, event={})", service, event);

        try {
            if (event instanceof IncomingEvent) {
                if (service.getServiceName().equals("WebSocketCommunicationService")) {
                    handleIncoming(service, (IncomingEvent) event, ctx);
                }

            } else if (event instanceof IncomingEventText) {
                IncomingEventText casted = (IncomingEventText) event;
                if (casted.getText() == null) {
                    service.answer(new MediaSession(casted.getSessionId(), null, null));
                } else if (service.getServiceName().equals("WebSocketCommunicationServiceText")) {
                    handleIncomingText(service, (IncomingEventText) event, ctx);
                }

            } else if (event instanceof AnsweredEvent) {
                // service.playMedia();
            }

        } catch (Exception ex) {
            logger.error("Error handling event: {}", event.getClass().getSimpleName(), ex);
        }
    }

    private void handleIncoming(CommunicationService service, IncomingEvent event, ChatBotContext ctx) {
        if (event.getTranslationService() != null && event.getAiService() == null) {
            // TODO pure translation service
        }

        EventHandler<BaseEvent> handler = (s, e) -> {
            try {
                if (e instanceof SpeechToTextEvent) {
                    String language = ctx.getLanguage();
                    SpeechToTextEvent se = (SpeechToTextEvent) e;
                    service.playMedia(event.getSessionId(), se.getData());
                    if (se.getResult() == SpeechToTextStatus.RECOGNIZED) {
                        service.playMedia(event.getSessionId(), "stop_recording");
                        String aiInput = se.getData().replaceAll("RECOGNIZED: ", "").replaceAll("(google)", "")
                                .replaceAll("(azure)", "").replace("()", "");
                        long l1 = System.currentTimeMillis();
                        if (language != "none" && language != null) {
                            Translation translation = getService(Translation.class, event.getTranslationService());
                            TranslationResult translationResult = translation.translation(aiInput, null, language);
                            service.playMedia(event.getSessionId(),
                                    aiInput + " -- WAS TRANSLATED TO --" + translationResult.getOutput());
                            aiInput = translationResult.getOutput();
                        }
                        long l2 = System.currentTimeMillis();
                        long translationTime = TimeUnit.MILLISECONDS.toSeconds(l2 - l1);
                        for (Openai ai : getServices(Openai.class)) {
                            if (ctx.getChosenAi().contains(ai.getProvider())) {
                                long t1 = System.currentTimeMillis();
                                OpenaiResult aiResult = ai.openai(aiInput, ctx, null, null);
                                long t2 = System.currentTimeMillis();
                                long openaiTime = TimeUnit.MILLISECONDS.toSeconds(t2 - t1);
                                service.playMedia(event.getSessionId(), aiResult.getResponse());

                                if (event.getOutputStream() == null) {
                                    // executes text-to-speech synchronously, and outputs the result as 1 big
                                    // byte-array
                                    for (TextToSpeech tts : getServices(TextToSpeech.class)) {
                                        logger.info(ctx.getChosenTts() + " : " + tts.getProvider());
                                        if (ctx.getChosenTts().contains(tts.getProvider())) {
                                            long a1 = System.currentTimeMillis();
                                            TextToSpeechByteResult ttsResult = ctx.getChosenTts().contains("google")
                                                    ? tts.textToStream(
                                                            ctx.getLanguage() != null && ctx.getLanguage() != "none"
                                                                    ? "en-US"
                                                                    : ctx.getLanguage(),
                                                            ttsLanguages.get(ctx.getLanguage()),
                                                            aiResult.getResponse().toString(),
                                                            null, null, null)
                                                    : tts.textToStream(
                                                            "en-US",
                                                            "en-US-AvaMultilingualNeural",
                                                            aiResult.getResponse().toString(),
                                                            null, null, null);
                                            long a2 = System.currentTimeMillis();
                                            long ttsTime = TimeUnit.MILLISECONDS.toSeconds(a2 - a1);
                                            service.playMedia(event.getSessionId(),
                                                    "Translation time: " + translationTime + " seconds. AI Time: "
                                                            + openaiTime
                                                            + " seconds. TTS time: " + ttsTime + " seconds.");
                                            service.playMedia(event.getSessionId(), ttsResult.getAudio());
                                        }
                                    }
                                } else {
                                    // executes text-to-speech asynchronously
                                    for (TextToSpeech tts : getServices(TextToSpeech.class)) {
                                        if (tts.getProvider() == ctx.getChosenTts()) {
                                            long a1 = System.currentTimeMillis();
                                            TextToSpeechByteResult ttsResult = tts.textToStream("en-US",
                                                    ttsVoices.get(ctx.getChosenTts()),
                                                    aiResult.getResponse().toString(),
                                                    null, null, event.getOutputStream());
                                            long a2 = System.currentTimeMillis();
                                            long ttsTime = TimeUnit.MILLISECONDS.toSeconds(a2 - a1);
                                            service.playMedia(event.getSessionId(),
                                                    "Translation time: " + translationTime + " seconds. AI Time: "
                                                            + openaiTime
                                                            + " seconds. TTS time: " + ttsTime + " seconds.");
                                            service.playMedia(event.getSessionId(), ttsResult.getAudio());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else {
                    logger.warn("Unhandled event type: {}", e.getClass().getSimpleName());
                }
            } catch (Exception ex) {
                logger.error("Exception when sending text to client", ex);
            }
        };

        try {
            for (SpeechToText stt : getServices(SpeechToText.class)) {
                if (stt.getProvider() == sttProviders.get(sttChosenProvider)) {
                    MediaSession session = stt.startSpeechToTextSession(event.getSessionId(), null, handler);
                    service.answer(session);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to start STT session for session ID: {}", event.getSessionId(), ex);
        }
    }

    private void handleIncomingText(CommunicationService service, IncomingEventText event,
            ChatBotContext ctx) {
        String[] text = { "" };
        EventHandler<BaseEvent> handler = (s, e) -> {
            try {
                if (e instanceof TextToSpeechEvent) {
                    String language = ctx.getLanguage();

                    if (!event.toString().isEmpty() && !text[0].equals(event.toString())) {
                        String aiInput = event.toString().replaceAll("RECOGNIZED: ", "").replaceAll("(google)", "")
                                .replaceAll("(azure)", "");
                        service.playMedia(event.getSessionId(), e.toString());
                        long l1 = System.currentTimeMillis();
                        if (language != "none" && language != null) {
                            Translation translation = getService(Translation.class, event.getTranslationService());
                            TranslationResult translationResult = translation.translation(aiInput, null, language);
                            service.playMedia(event.getSessionId(),
                                    aiInput + " -- WAS TRANSLATED TO --" + translationResult.getOutput());
                            aiInput = translationResult.getOutput();
                        }
                        long l2 = System.currentTimeMillis();
                        long translationTime = TimeUnit.MILLISECONDS.toSeconds(l2 - l1);
                        if (!event.toString().isEmpty() && !text[0].equals(event.toString())) {
                            for (Openai ai : getServices(Openai.class)) {
                                if (ctx.getChosenAi().contains(ai.getProvider())) {
                                    long t1 = System.currentTimeMillis();
                                    OpenaiResult aiResult = ai.openai(aiInput, ctx, null, null);
                                    long t2 = System.currentTimeMillis();
                                    long openaiTime = TimeUnit.MILLISECONDS.toSeconds(t2 - t1);
                                    service.playMedia(event.getSessionId(), aiResult.getResponse());

                                    service.playMedia(event.getSessionId(), e.toString());
                                    if (event.getOutputStream() == null) {
                                        // executes text-to-speech synchronously, and outputs the result as 1 big
                                        // byte-array
                                        for (TextToSpeech tts : getServices(TextToSpeech.class)) {
                                            if (ctx.getChosenTts().contains(tts.getProvider())) {
                                                long a1 = System.currentTimeMillis();
                                                TextToSpeechByteResult ttsResult = ctx.getChosenTts().contains("google")
                                                        ? tts.textToStream(
                                                                ctx.getLanguage() != null && ctx.getLanguage() != "none"
                                                                        ? "en-US"
                                                                        : ctx.getLanguage(),
                                                                ttsLanguages.get(ctx.getLanguage()),
                                                                aiResult.getResponse().toString(),
                                                                null, null, null)
                                                        : tts.textToStream(
                                                                "en-US",
                                                                "en-US-AvaMultilingualNeural",
                                                                aiResult.getResponse().toString(),
                                                                null, null, null);
                                                long a2 = System.currentTimeMillis();
                                                long ttsTime = TimeUnit.MILLISECONDS.toSeconds(a2 - a1);
                                                service.playMedia(event.getSessionId(),
                                                        " AI Time: "
                                                                + openaiTime
                                                                + " seconds. TTS time: " + ttsTime + " seconds.");
                                                service.playMedia(event.getSessionId(), ttsResult.getAudio());
                                            }
                                        }
                                        text[0] = event.toString();
                                    } else {
                                        // executes text-to-speech synchronously, and outputs the result as 1 big
                                        // byte-array
                                        for (TextToSpeech tts : getServices(TextToSpeech.class)) {
                                            if (tts.getProvider() == ctx.getChosenTts()) {
                                                long a1 = System.currentTimeMillis();
                                                TextToSpeechByteResult ttsResult = tts.textToStream("en-US",
                                                        ttsVoices.get(ctx.getChosenTts()),
                                                        aiResult.getResponse().toString(),
                                                        null, null, event.getOutputStream());
                                                long a2 = System.currentTimeMillis();
                                                long ttsTime = TimeUnit.MILLISECONDS.toSeconds(a2 - a1);
                                                service.playMedia(event.getSessionId(),
                                                        "Translation time: " + translationTime + " seconds. AI Time: "
                                                                + openaiTime
                                                                + " seconds. TTS time: " + ttsTime + " seconds.");
                                                service.playMedia(event.getSessionId(), ttsResult.getAudio());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

                else {
                    logger.warn("Unhandled event type here: {}", e.getClass().getSimpleName());
                }
            } catch (Exception ex) {
                logger.error("Exception when sending text to client", ex);
            }
        };

        try {
            for (TextToSpeech tts : getServices(TextToSpeech.class)) {
                if (tts.getProvider() == ttsProviders.get(ttsChosenProvider)) {
                    MediaSession session = tts.startTextToSpeechSession(event.getSessionId(), event.toString(), null,
                            handler);
                    service.answer(session);
                }

            }
        } catch (Exception ex) {
            logger.error("Failed to start TTS session for session ID: {}", event.getSessionId(), ex);
        }

    }

    @SuppressWarnings("unchecked")
    public <T extends Service> List<T> getServices(Class<T> clazz) {
        return services.values().stream()
                .filter(clazz::isInstance)
                .map(c -> (T) c)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends Service> T getService(Class<T> clazz, String serviceName) {
        return services.values().stream()
                .filter(clazz::isInstance)
                .filter(c -> serviceName == null || Objects.equals(serviceName, c.getServiceName()))
                .map(c -> (T) c)
                .findFirst()
                .orElse(null);
    }
}
