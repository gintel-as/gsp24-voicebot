package com.gintel.cognitiveservices.core.tts;

import com.gintel.cognitiveservices.core.tts.types.InputFormat;
import com.gintel.cognitiveservices.core.tts.types.OutputFormat;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;

public interface TextToSpeech {
    TextToSpeechResult textToSpeech(String language, String voiceName, String text,
            InputFormat input, OutputFormat output);
}
