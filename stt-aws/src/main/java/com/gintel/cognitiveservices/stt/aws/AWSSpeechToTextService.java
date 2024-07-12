package com.gintel.cognitiveservices.stt.aws;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.stt.SpeechToText;
import com.gintel.cognitiveservices.core.stt.SpeechToTextEvent;
import com.gintel.cognitiveservices.core.stt.types.InputFormat;
import com.gintel.cognitiveservices.core.stt.types.OutputFormat;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextResult;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextStatus;

public class AWSSpeechToTextService implements SpeechToText {
    private static final Logger logger = LoggerFactory.getLogger(AWSSpeechToTextService.class);

    @Override
    public String getProvider() {
        return "aws";
    }

    private AWSSTTConfig serviceConfig;

    public AWSSpeechToTextService(AWSSTTConfig serviceConfig) {
        this.serviceConfig = serviceConfig;

        logger.info("region is {}", serviceConfig.region());
    }

    @Override
    public SpeechToTextResult speechToText(String language, InputFormat input, OutputFormat output) {

        String serviceRegion = serviceConfig.region();

        String lang = "nb-NO";

        if (language != null) {
            lang = language.replace(new StringBuilder().append('"'), "");
        }

        try {

        } catch (Exception ex) {
            logger.error("Exception in speechToText", ex);
        }
        return new SpeechToTextResult(SpeechToTextStatus.ERROR, null, null);
    }

    @Override
    public MediaSession startSpeechToTextSession(String sessionId, String language,
            EventHandler<BaseEvent> eventHandler) {

        logger.info("createSession(sessionId={}, language={})", sessionId, language);

        String serviceRegion = serviceConfig.region();

        try {

            return new MediaSession(sessionId, eventHandler, new MediaStream() {
                @Override
                public void write(byte[] data) {

                }

                @Override
                public void close() {

                }

                @Override
                public void write(String data) {
                    // TODO Auto-generated method stub
                    throw new UnsupportedOperationException("Unimplemented method 'write'");
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Exception in speechToTextSession", ex);
        }
    }
}
