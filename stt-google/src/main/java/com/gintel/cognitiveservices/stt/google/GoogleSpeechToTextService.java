package com.gintel.cognitiveservices.stt.google;

import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;

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
import com.google.protobuf.Duration;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.TargetDataLine;

public class GoogleSpeechToTextService implements SpeechToText{
    private static final Logger logger = LoggerFactory.getLogger(GoogleSpeechToTextService.class);
    

    @Override
    public SpeechToTextResult speechToText(String language, InputFormat input, OutputFormat output) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'speechToText'");
    }

    @Override
    public MediaSession startSpeechToTextSession(String sessionId, String language, EventHandler<BaseEvent> eventHandler) {

        logger.info("createSession(sessionId={}, language={})", sessionId, language);

        try {
            ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
            try (SpeechClient client = SpeechClient.create()) {

                responseObserver =
                    new ResponseObserver<StreamingRecognizeResponse>() {
                    ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                    public void onStart(StreamController controller) {}

                    public void onResponse(StreamingRecognizeResponse response) {
                        responses.add(response);
                    }

                    public void onComplete() {
                        for (StreamingRecognizeResponse response : responses) {
                        StreamingRecognitionResult result = response.getResultsList().get(0);
                        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                        System.out.printf("Transcript : %s\n", alternative.getTranscript());
                        }
                    }

                    public void onError(Throwable t) {
                        System.out.println(t);
                    }
                    };

                ClientStream<StreamingRecognizeRequest> clientStream =
                    client.streamingRecognizeCallable().splitCall(responseObserver);

                RecognitionConfig recognitionConfig =
                    RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setLanguageCode("en-US")
                        .setSampleRateHertz(16000)
                        .build();
                StreamingRecognitionConfig streamingRecognitionConfig =
                    StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build();

                StreamingRecognizeRequest request =
                    StreamingRecognizeRequest.newBuilder()
                        .setStreamingConfig(streamingRecognitionConfig)
                        .build(); // The first request in a streaming call has to be a config

                clientStream.send(request);
                // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed: true,
                // bigEndian: false
                AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info targetInfo =
                    new Info(
                        TargetDataLine.class,
                        audioFormat); // Set the system information to read from the microphone audio stream

                if (!AudioSystem.isLineSupported(targetInfo)) {
                System.out.println("Microphone not supported");
                System.exit(0);
                }
                // Target data line captures the audio stream the microphone produces.
                TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
                targetDataLine.open(audioFormat);
                targetDataLine.start();
                System.out.println("Start speaking");
                long startTime = System.currentTimeMillis();
                // Audio Input Stream
                AudioInputStream audio = new AudioInputStream(targetDataLine);
                while (true) {
                long estimatedTime = System.currentTimeMillis() - startTime;
                byte[] data = new byte[6400];
                audio.read(data);
                if (estimatedTime > 60000) { // 60 seconds
                    System.out.println("Stop speaking.");
                    targetDataLine.stop();
                    targetDataLine.close();
                    break;
                }
                request =
                    StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(data))
                        .build();
                clientStream.send(request);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            responseObserver.onComplete();

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
            });
        } catch (Exception ex) {
            throw new RuntimeException("Exception in speechToTextSession", ex);
        }
    }
    
}
