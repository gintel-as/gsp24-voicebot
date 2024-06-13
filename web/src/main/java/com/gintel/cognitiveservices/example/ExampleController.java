package com.gintel.cognitiveservices.example;

import java.util.List;

import com.gintel.cognitiveservices.config.WebConfig;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;

public class ExampleController {
    private WebConfig config;
    private List<TextToSpeech> ttsServices;

    public ExampleController(WebConfig config, List<TextToSpeech> ttsServices) {
        this.config = config;
        this.ttsServices = ttsServices;
    }

    public TextToSpeechResult textToSpeech(String language, String voiceName, String text) {
        for (TextToSpeech impl : ttsServices) {
            // for now, just pick the first one
            return impl.textToSpeech(language, voiceName, text, null, null);            
        }
        throw new RuntimeException("No tts implementations found");
    }
}
