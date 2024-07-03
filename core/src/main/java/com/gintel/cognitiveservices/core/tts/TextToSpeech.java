package com.gintel.cognitiveservices.core.tts;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;

import com.gintel.cognitiveservices.core.tts.types.InputFormat;
import com.gintel.cognitiveservices.core.tts.types.OutputFormat;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechByteResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.service.Service;

public interface TextToSpeech extends Service {
    TextToSpeechResult textToSpeech(String language, String voiceName, String text, InputFormat input, OutputFormat output);
    
    TextToSpeechByteResult textToStream(String language, String voiceName, String text,
            InputFormat input, OutputFormat output, MediaStream outputStream);

    MediaSession startTextToSpeechSession(String sessionId, String text, String language, EventHandler<BaseEvent> handler);

}
