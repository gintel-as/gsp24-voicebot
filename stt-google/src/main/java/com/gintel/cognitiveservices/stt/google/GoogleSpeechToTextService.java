package com.gintel.cognitiveservices.stt.google;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;

public class GoogleSpeechToTextService implements SpeechToText {
    private static final Logger logger = LoggerFactory.getLogger(GoogleSpeechToTextService.class);

    @Override
    public SpeechToTextResult speechToText(String language, InputFormat input, OutputFormat output) {
        // Implementation for one-time speech recognition (not continuous)
        throw new UnsupportedOperationException("Unimplemented method 'speechToText'");
    }

    @Override
    public MediaSession startSpeechToTextSession(String sessionId, String language, EventHandler<BaseEvent> eventHandler) {
        logger.info("createSession(sessionId={}, language={})", sessionId, language);

        try {
            SpeechClient client = SpeechClient.create();

            // Configure recognition settings
            RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("en-US")
                    .setSampleRateHertz(16000)
                    .setEnableAutomaticPunctuation(true)
                    .setProfanityFilter(true)
                    .build();

            StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig)
                    .build();

            // Create a queue to handle audio data
            BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>();

            // Response observer to handle server responses
            ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {
                @Override
                public void onStart(StreamController controller) {
                    logger.info("Stream started");
                }

                @Override
                public void onResponse(StreamingRecognizeResponse response) {
                    try {
                        StreamingRecognitionResult result = response.getResultsList().get(0);
                        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                        String transcript = alternative.getTranscript();
                        if (result.getIsFinal()) {
                            eventHandler.onEvent(this, new SpeechToTextEvent("RECOGNIZED: " + transcript, SpeechToTextStatus.RECOGNIZED));
                        } else {
                            eventHandler.onEvent(this, new SpeechToTextEvent("RECOGNIZED: " + transcript, SpeechToTextStatus.RECOGNIZING));
                        }
                    } catch (Exception e) {
                        logger.error("Error processing response", e);
                    }
                }

                @Override
                public void onComplete() {
                    logger.info("Stream completed");
                    eventHandler.onEvent(this, new SpeechToTextEvent("Session stopped event.", SpeechToTextStatus.STOPPED));
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("Stream error", t);
                    eventHandler.onEvent(this, new SpeechToTextEvent("ERROR: " + t.getMessage(), SpeechToTextStatus.ERROR));
                }
            };

            // Create a client stream for sending requests
            ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

            // Send the initial configuration request
            StreamingRecognizeRequest initialRequest = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingRecognitionConfig)
                    .build();
            clientStream.send(initialRequest);

            // MediaStream to handle input audio data
            MediaStream mediaStream = new MediaStream() {
                @Override
                public void write(byte[] data) {
                    try {
                        audioQueue.put(data);
                    } catch (InterruptedException e) {
                        logger.error("Error writing to audio queue", e);
                        Thread.currentThread().interrupt();
                    }
                }

                @Override
                public void close() {
                    clientStream.closeSend();
                }
            };

            // Thread to continuously send audio data to the Google API
            new Thread(() -> {
                try {
                    while (true) {
                        byte[] audioData = audioQueue.poll(50, TimeUnit.MILLISECONDS);
                        if (audioData != null) {
                            StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                                    .setAudioContent(ByteString.copyFrom(audioData))
                                    .build();
                            clientStream.send(request);
                        } else {
                            // Send silence if no audio data is available
                            byte[] silence = new byte[320]; // 320 bytes of silence (20ms of audio at 16kHz, 16-bit PCM)
                            StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                                    .setAudioContent(ByteString.copyFrom(silence))
                                    .build();
                            clientStream.send(request);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("Audio sending interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Error in audio sending thread", e);
                }
            }).start();

            return new MediaSession(sessionId, eventHandler, mediaStream);

        } catch (Exception e) {
            logger.error("Exception in speechToTextSession", e);
            throw new RuntimeException("Exception in speechToTextSession", e);
        }
    }
}
