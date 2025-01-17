package com.gintel.cognitiveservices.stt.azure;

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
import com.microsoft.cognitiveservices.speech.AutoDetectSourceLanguageConfig;
import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;

public class AzureSpeechToTextService implements SpeechToText {
    private static final Logger logger = LoggerFactory.getLogger(AzureSpeechToTextService.class);

    @Override
    public String getProvider() {
        return "azure";
    }

    private AzureSTTConfig serviceConfig;

    public AzureSpeechToTextService(AzureSTTConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    // One-time speech-recognition API with microphone input
    @Override
    public SpeechToTextResult speechToText(String language, InputFormat input, OutputFormat output) {

        String serviceRegion = serviceConfig.region();

        String lang = "nb-NO";

        if (language != null) {
            lang = language.replace(new StringBuilder().append('"'), "");
        }

        // SpeechConfig config =
        // SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(),
        // serviceConfig.region());
        // config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio24Khz96KBitRateMonoMp3);
        // if (voiceName != null) {
        // config.setSpeechSynthesisVoiceName(voiceName);
        // }
        // AudioConfig audioConfig = AudioConfig.fromWavFileOutput("output/output.mp3");
        // SpeechRecognizer reco = new SpeechRecognizer(config, "nb-NO");

        try (SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(), serviceRegion);
                SpeechRecognizer reco = new SpeechRecognizer(config, lang)) {
            assert (config != null);
            assert (reco != null);

            System.out.println("Say something...");

            Future<com.microsoft.cognitiveservices.speech.SpeechRecognitionResult> task = reco.recognizeOnceAsync();
            assert (task != null);

            com.microsoft.cognitiveservices.speech.SpeechRecognitionResult result = task.get();
            assert (result != null);

            if (result.getReason() == ResultReason.RecognizedSpeech) {
                System.out.println("We recognized: " + result.getText());
                return new SpeechToTextResult(SpeechToTextStatus.RECOGNIZED, result.getText(),
                        reco.getSpeechRecognitionLanguage());
            } else if (result.getReason() == ResultReason.NoMatch) {
                System.out.println("NOMATCH: Speech could not be recognized.");
                return new SpeechToTextResult(SpeechToTextStatus.ERROR, null, null);
            } else if (result.getReason() == ResultReason.Canceled) {
                CancellationDetails cancellation = CancellationDetails.fromResult(result);
                System.out.println("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    System.out.println("CANCELED: Did you update the subscription info?");
                }
                return new SpeechToTextResult(SpeechToTextStatus.ERROR, null, null);
            }
        } catch (Exception ex) {
            logger.error("Exception in speechToText", ex);
        }
        return new SpeechToTextResult(SpeechToTextStatus.ERROR, null, null);
    }

    // Speech-recognition session used for continous transcription
    @Override
    public MediaSession startSpeechToTextSession(String sessionId, String language,
            EventHandler<BaseEvent> eventHandler) {

        String serviceRegion = serviceConfig.region();

        try {
            PushAudioInputStream is = AudioInputStream.createPushStream();
            AudioConfig audioCfg = AudioConfig.fromStreamInput(is);

            // Defines list of languages which can be recognized automatically
            AutoDetectSourceLanguageConfig autoDetectLanguages = AutoDetectSourceLanguageConfig
                    .fromLanguages(Arrays.asList("en-US", "nb-NO", "es-ES"));

            if (language != null) {
                autoDetectLanguages = AutoDetectSourceLanguageConfig.fromLanguages(Arrays.asList(language));
            }

            SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(),
                    serviceRegion);
            // config.setProperty(PropertyId.Speech_SegmentationSilenceTimeoutMs,"2000");
            // Set timout after end of speech before finishing segment (default is 500)
            config.setProperty(PropertyId.SpeechServiceConnection_LanguageIdMode, "Continuous");
            SpeechRecognizer recognizer = new SpeechRecognizer(config, autoDetectLanguages, audioCfg);

            recognizer.recognizing.addEventListener((s, e) -> {
                eventHandler.onEvent(s, new SpeechToTextEvent("RECOGNIZING: " + e.getResult().getText(),
                        SpeechToTextStatus.RECOGNIZING));
            });

            recognizer.recognized.addEventListener((s, e) -> {
                if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                    eventHandler.onEvent(s, new SpeechToTextEvent("RECOGNIZED: " + e.getResult().getText() + " (azure)",
                            SpeechToTextStatus.RECOGNIZED));
                } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    eventHandler.onEvent(s,
                            new SpeechToTextEvent("NOMATCH: Speech could not be recognized.",
                                    SpeechToTextStatus.ERROR));
                }
            });

            recognizer.canceled.addEventListener((s, e) -> {
                String result = "CANCELED: Reason=" + e.getReason();

                if (e.getReason() == CancellationReason.Error) {
                    result += "CANCELED: ErrorCode=" + e.getErrorCode() + "\n";
                    result += "CANCELED: ErrorDetails=" + e.getErrorDetails() + "\n";
                    result += "CANCELED: Did you update the subscription info?";
                }
                eventHandler.onEvent(s, new SpeechToTextEvent(result, SpeechToTextStatus.ERROR));
            });

            recognizer.sessionStarted.addEventListener((s, e) -> {
                eventHandler.onEvent(s, new SpeechToTextEvent("Session started event.",
                        SpeechToTextStatus.STARTED));
            });

            recognizer.sessionStopped.addEventListener((s, e) -> {
                eventHandler.onEvent(s, new SpeechToTextEvent("Session stopped event.",
                        SpeechToTextStatus.STOPPED));
            });

            recognizer.startContinuousRecognitionAsync();

            return new MediaSession(sessionId, eventHandler, new MediaStream() {
                @Override
                public void write(byte[] data) {
                    is.write(data);
                }

                @Override
                public void close() {
                    is.close();
                    recognizer.close();
                    config.close();
                }

                @Override
                public void write(String data) {
                    throw new UnsupportedOperationException("Unimplemented method 'write' for String input");
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Exception in speechToTextSession", ex);
        }
    }
}
