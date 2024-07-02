package com.gintel.cognitiveservices.example;

import java.util.List;

import com.gintel.cognitiveservices.config.WebConfig;
import com.gintel.cognitiveservices.core.openai.Openai;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.core.openai.types.OpenaiResult;
import com.gintel.cognitiveservices.core.stt.SpeechToText;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextResult;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;

public class ExampleController {
    @SuppressWarnings("unused")
    private WebConfig config;
    private List<TextToSpeech> ttsServices;
    private List<SpeechToText> sttServices;
    private List<Openai> openaiServices;

    public ExampleController(WebConfig config, List<TextToSpeech> ttsServices, List<SpeechToText> sttServices, List<Openai> openaiServices) {
        this.config = config;
        this.ttsServices = ttsServices;
        this.sttServices = sttServices;
        this.openaiServices = openaiServices;
    }

    public TextToSpeechResult textToSpeech(String language, String voiceName, String text) {
        for (TextToSpeech impl : ttsServices) {
            // for now, just pick the first one
            return impl.textToSpeech(language, voiceName, text, null, null);            
        }
        throw new RuntimeException("No tts implementations found");
    }

    public SpeechToTextResult speechToText(String language) {
        for (SpeechToText impl : sttServices) {
            // for now, just pick the first one
            return impl.speechToText(language, null, null);            
        }
        throw new RuntimeException("No stt implementations found");
    }

    public OpenaiResult openai(String text, ChatBotContext ctx){
        for (Openai impl : openaiServices) {
            // for now, just pick the first one
            return impl.openai(text, ctx, null, null); 
        }
        throw new RuntimeException("No openai implementations found");
    }
}
