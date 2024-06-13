package com.gintel.cognitiveservices.example;

import java.util.List;

import com.gintel.cognitiveservices.config.WebConfig;
import com.gintel.cognitiveservices.core.stt.SpeechToText;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextResult;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;

public class ExampleController {
    private WebConfig config;
    private List<TextToSpeech> ttsServices;
    private List<SpeechToText> sttServices;

    public ExampleController(WebConfig config, List<TextToSpeech> ttsServices, List<SpeechToText> sttServices) {
        this.config = config;
        this.ttsServices = ttsServices;
        this.sttServices = sttServices;
    }

    public TextToSpeechResult textToSpeech(String language, String voiceName, String text) {
        for (TextToSpeech impl : ttsServices) {
            // for now, just pick the first one
            return impl.textToSpeech(language, voiceName, text, null, null);            
        }
        throw new RuntimeException("No tts implementations found");
    }

    public SpeechToTextResult speechToText(String language, String voiceName, String text) {
        for (SpeechToText impl : sttServices) {
            // for now, just pick the first one
            return impl.speechToText(language, voiceName, text, null, null);            
        }
        throw new RuntimeException("No stt implementations found");
    }
}
