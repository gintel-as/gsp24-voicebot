package com.gintel.cognitiveservices.example;

import com.gintel.cognitiveservices.config.WebConfig;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.tts.azure.AzureTextToSpeechService;

public class ExampleController {
    private WebConfig config;

    public ExampleController(WebConfig config) {
        this.config = config;
    }

    public TextToSpeechResult textToSpeech(String language, String voiceName, String text) {
        AzureTextToSpeechService textToSpeech = new AzureTextToSpeechService();
        return textToSpeech.textToSpeech(language, voiceName, text, null, null);
    }
}
