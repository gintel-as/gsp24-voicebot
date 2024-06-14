package com.gintel.cognitiveservices.core.stt;

import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.stt.types.InputFormat;
import com.gintel.cognitiveservices.core.stt.types.OutputFormat;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextResult;
import com.gintel.cognitiveservices.service.Service;

public interface SpeechToText extends Service {
    SpeechToTextResult speechToText(String language, InputFormat input, OutputFormat output);

    MediaSession startSpeechToTextSession(String sessionId, String language,
            EventHandler<BaseEvent> eventHandler);
}
