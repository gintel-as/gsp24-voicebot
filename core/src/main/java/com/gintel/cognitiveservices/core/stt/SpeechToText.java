package com.gintel.cognitiveservices.core.stt;

import com.gintel.cognitiveservices.core.stt.types.SpeechToTextResult;
import com.gintel.cognitiveservices.core.stt.types.InputFormat;
import com.gintel.cognitiveservices.core.stt.types.OutputFormat;


public interface SpeechToText {
    SpeechToTextResult speechToText(String language, InputFormat input, OutputFormat output);
}