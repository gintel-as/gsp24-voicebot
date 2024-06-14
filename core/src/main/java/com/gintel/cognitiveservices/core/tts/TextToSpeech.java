package com.gintel.cognitiveservices.core.tts;

import com.gintel.cognitiveservices.core.tts.types.InputFormat;
import com.gintel.cognitiveservices.core.tts.types.OutputFormat;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.service.Service;

public interface TextToSpeech extends Service {
    TextToSpeechResult textToSpeech(String language, String voiceName, String text,
            InputFormat input, OutputFormat output);
}
