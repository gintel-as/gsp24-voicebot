import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.FieldMask;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.gax.core.FixedCredentialsProvider;

import java.io.FileOutputStream;
public class GoogleTextToSpeechService {

    private TextToSpeechSettings settings;
    private TextToSpeechClient client;

    public GoogleTextToSpeechService(String credentialsPath) throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
        .createScoped("https://www.googleapis.com/auth/cloud-platform");

        settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(() -> GoogleCredentials.fromStream(new FileInputStream(credentialsPath)))
                .build();
        client = TextToSpeechClient.create(settings);
    }

    public void textToSpeech(String text, String languageCode, String voiceName, String outputFormat, String outputFile) {
        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(languageCode)
                .setName(voiceName)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.valueOf(outputFormat))
                .build();

        SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

        ByteString audioContents = response.getAudioContent();

        try (OutputStream out = new FileOutputStream(outputFile)) {
            out.write(audioContents.toByteArray());
            System.out.println("Audio content written to file " + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void textToStream(String text, String languageCode, String voiceName, String outputFormat, OutputStream outputStream) {
        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(languageCode)
                .setName(voiceName)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.valueOf(outputFormat))
                .build();

        SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

        ByteString audioContents = response.getAudioContent();

        try {
            outputStream.write(audioContents.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTextToSpeechSession(String sessionId, String text, String languageCode, String voiceName, String outputFormat, EventHandler eventHandler) {
        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(languageCode)
                .setName(voiceName)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.valueOf(outputFormat))
                .build();

        BidiStreamingCallable<SynthesizeSpeechRequest, SynthesizeSpeechResponse> callable = client.synthesizeSpeechCallable();

        ApiStreamObserver<SynthesizeSpeechResponse> responseObserver = new ApiStreamObserver<SynthesizeSpeechResponse>() {
            @Override
            public void onNext(SynthesizeSpeechResponse response) {
                ByteString audioContents = response.getAudioContent();
                eventHandler.onAudioChunk(audioContents.toByteArray());
            }

            @Override
            public void onError(Throwable t) {
                eventHandler.onError(t);
            }

            @Override
            public void onCompleted() {
                eventHandler.onComplete();
            }
        };

        ApiStreamObserver<SynthesizeSpeechRequest> requestObserver = callable.bidiStreamingCall(responseObserver);
        requestObserver.onNext(SynthesizeSpeechRequest.newBuilder().setInput(input).setVoice(voice).setAudioConfig(audioConfig).build());
        requestObserver.onCompleted();
    }

    public interface EventHandler {
        void onAudioChunk(byte[] audioChunk);

        void onError(Throwable t);

        void onComplete();
    }
}
