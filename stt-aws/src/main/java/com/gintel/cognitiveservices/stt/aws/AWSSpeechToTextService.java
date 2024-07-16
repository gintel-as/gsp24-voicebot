package com.gintel.cognitiveservices.stt.aws;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.stt.SpeechToText;
import com.gintel.cognitiveservices.core.stt.types.InputFormat;
import com.gintel.cognitiveservices.core.stt.types.OutputFormat;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextResult;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;
import software.amazon.awssdk.services.transcribestreaming.model.AudioEvent;
import software.amazon.awssdk.services.transcribestreaming.model.AudioStream;
import software.amazon.awssdk.services.transcribestreaming.model.MediaEncoding;
import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionRequest;
import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionResponseHandler;

public class AWSSpeechToTextService implements SpeechToText {
    private static final Logger logger = LoggerFactory.getLogger(AWSSpeechToTextService.class);

    @Override
    public String getProvider() {
        return "aws";
    }

    private AWSSTTConfig serviceConfig;

    private static final int SAMPLE_RATE = 16000;

    private TranscribeStreamingRetryClient client;

    public AWSSpeechToTextService(AWSSTTConfig serviceConfig) {
        this.serviceConfig = serviceConfig;

        logger.info("aws region is {}", serviceConfig.region());
    }

    private static class AudioStreamPublisher implements Publisher<AudioStream> {
        private final InputStream inputStream;
        private static Subscription currentSubscription;

        private AudioStreamPublisher(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void subscribe(Subscriber<? super AudioStream> s) {
            if (AudioStreamPublisher.currentSubscription == null) {
                AudioStreamPublisher.currentSubscription = new SubscriptionImpl(s, inputStream);
            } else {
                AudioStreamPublisher.currentSubscription.cancel();
                AudioStreamPublisher.currentSubscription = new SubscriptionImpl(s, inputStream);
            }
            s.onSubscribe(currentSubscription);
        }
    }

    public static class SubscriptionImpl implements Subscription {
        private static final int CHUNK_SIZE_IN_BYTES = 1024 * 1;
        private final Subscriber<? super AudioStream> subscriber;
        private final InputStream inputStream;
        private ExecutorService executor = Executors.newFixedThreadPool(1);
        private AtomicLong demand = new AtomicLong(0);

        SubscriptionImpl(Subscriber<? super AudioStream> s, InputStream inputStream) {
            this.subscriber = s;
            this.inputStream = inputStream;
        }

        @Override
        public void request(long n) {
            if (n <= 0) {
                subscriber.onError(new IllegalArgumentException("Demand must be positive"));
            }

            demand.getAndAdd(n);

            executor.submit(() -> {
                try {
                    do {
                        ByteBuffer audioBuffer = getNextEvent();
                        if (audioBuffer.remaining() > 0) {
                            AudioEvent audioEvent = audioEventFromBuffer(audioBuffer);
                            subscriber.onNext(audioEvent);
                        } else {
                            subscriber.onComplete();
                            break;
                        }
                    } while (demand.decrementAndGet() > 0);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            });
        }

        @Override
        public void cancel() {
            executor.shutdown();
        }

        private ByteBuffer getNextEvent() {
            ByteBuffer audioBuffer = null;
            byte[] audioBytes = new byte[CHUNK_SIZE_IN_BYTES];

            int len = 0;
            try {
                len = inputStream.read(audioBytes);

                if (len <= 0) {
                    audioBuffer = ByteBuffer.allocate(0);
                } else {
                    audioBuffer = ByteBuffer.wrap(audioBytes, 0, len);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return audioBuffer;
        }

        private AudioEvent audioEventFromBuffer(ByteBuffer bb) {
            return AudioEvent.builder()
                    .audioChunk(SdkBytes.fromByteBuffer(bb))
                    .build();
        }
    }

    public StartStreamTranscriptionResponseHandler getResponseHandler(EventHandler<BaseEvent> eventHandler) {
        StreamTranscriptionBehaviorImpl impl = new StreamTranscriptionBehaviorImpl(eventHandler);
        final StartStreamTranscriptionResponseHandler build = StartStreamTranscriptionResponseHandler.builder()
                .onResponse(r -> {
                    impl.onResponse(r);
                })
                .onError(e -> {
                    impl.onError(e);
                })
                .onComplete(() -> {
                    impl.onComplete();
                })

                .subscriber(event -> impl.onStream(event))
                .build();
        return build;
    }

    @Override
    public SpeechToTextResult speechToText(String language, InputFormat input, OutputFormat output) {
        // Implementation for one-time speech recognition (not continuous)
        // For now, we can throw an unsupported operation exception or leave it
        // unimplemented
        throw new UnsupportedOperationException("Unimplemented method 'speechToText'");
    }

    @Override
    public MediaSession startSpeechToTextSession(String sessionId, String language,
            EventHandler<BaseEvent> eventHandler) {

        logger.info("createSession(sessionId={}, language={})", sessionId, language);

        String lang = "en-US"; // Default language code
        if (language != null) {
            lang = language.replace("\"", "");
        }

        String endpoint = "https://transcribestreaming."
                + serviceConfig.region().toString().toLowerCase().replace('_', '-') + ".amazonaws.com";

        try {

            ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.builder()
                    .profileName("herman_ostengen")
                    .build();
            TranscribeStreamingAsyncClient client = TranscribeStreamingAsyncClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .endpointOverride(new URI(endpoint))
                    .region(Region.of(serviceConfig.region()))
                    .build();

            StartStreamTranscriptionRequest request = StartStreamTranscriptionRequest.builder()
                    .languageCode(lang)
                    .mediaEncoding(MediaEncoding.PCM)
                    .mediaSampleRateHertz(SAMPLE_RATE)
                    .build();

            CompletableFuture<Void> result = client.startStreamTranscription(
                    request,
                    new AudioStreamPublisher(System.in),
                    getResponseHandler(eventHandler));

            result.get();

            return new MediaSession(sessionId, eventHandler, new MediaStream() {
                @Override
                public void write(byte[] data) {
                    // Implement if needed to write audio data to the stream
                }

                @Override
                public void close() {
                    client.close();
                }

                @Override
                public void write(String data) {
                    // Unimplemented method
                    throw new UnsupportedOperationException("Unimplemented method 'write'");
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException("Exception in startSpeechToTextSession", ex);
        }
    }

}
