package com.gintel.cognitiveservices.tts.google;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.FieldMask;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;


import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.TextToSpeechEvent;
import com.gintel.cognitiveservices.core.tts.types.InputFormat;
import com.gintel.cognitiveservices.core.tts.types.OutputFormat;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechByteResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechStatus;
import com.google.api.gax.core.FixedCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileOutputStream;
public class GoogleTextToSpeechService implements TextToSpeech {
    private static final Logger logger = LoggerFactory.getLogger(GoogleTextToSpeechService.class);
    
    private TextToSpeechClient client;
    private String credentialsPath;

    public GoogleTextToSpeechService() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
            client = TextToSpeechClient.create(settings);
        } catch (IOException e) {
            logger.error("Failed to initialize GoogleTextToSpeechService", e);
            throw new RuntimeException("Failed to initialize GoogleTextToSpeechService", e);
        }
    }

    

    @Override
    public TextToSpeechResult textToSpeech(String language, String voiceName, String text, InputFormat input, OutputFormat output) {
        return synthesizeTextToByteArray(language, voiceName, text, AudioEncoding.valueOf(output.name()));
    }

    @Override
    public TextToSpeechByteResult textToStream(String language, String voiceName, String text, InputFormat input, OutputFormat output, MediaStream outputStream) {
        return synthesizeTextToStream(language, voiceName, text, AudioEncoding.valueOf(output.name()), outputStream);
    }

    private TextToSpeechResult synthesizeTextToByteArray(String languageCode, String voiceName, String text, AudioEncoding audioEncoding) {
        SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(languageCode)
                .setName(voiceName)
                .build();
        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(audioEncoding)
                .build();

        SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);
        ByteString audioContents = response.getAudioContent();

        byte[] audioData = audioContents.toByteArray();
        byte[] trimmedAudioData = new byte[Math.min(1000, audioData.length)];
        System.arraycopy(audioData, 0, trimmedAudioData, 0, trimmedAudioData.length);

        return new TextToSpeechResult(TextToSpeechStatus.OK, trimmedAudioData, null);
    }

    private TextToSpeechByteResult synthesizeTextToStream(String languageCode, String voiceName, String text, AudioEncoding audioEncoding, MediaStream outputStream) {
        Executors.newSingleThreadExecutor().submit(() -> {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setName(voiceName)
                    .build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(audioEncoding)
                    .build();

            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();
            byte[] buffer = audioContents.toByteArray();

            outputStream.write(buffer);
        });

        return new TextToSpeechByteResult(TextToSpeechStatus.OK, null);
    }
    @Override
    public MediaSession startTextToSpeechSession(String sessionId, String text, String language, EventHandler<BaseEvent> eventHandler) {
        logger.info("createSession(sessionId={}, language={})", sessionId, language);

        try {
             // Replace with your region
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/cloud-platform");

            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            TextToSpeechClient client = TextToSpeechClient.create(settings);


            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(language)
                    .setName("en-US-Wavenet-D")
                    .build();
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
            .setAudioEncoding(AudioEncoding.LINEAR16) // Adjust encoding as needed
            .build();

    SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);
    ByteString audioContents = response.getAudioContent();

    int chunkSize = 1024; // Adjust chunk size as needed
    for (int i = 0; i < audioContents.size(); i += chunkSize) {
        int end = Math.min(audioContents.size(), i + chunkSize);
        byte[] chunk = audioContents.substring(i, end).toByteArray();
        eventHandler.onEvent(new TextToSpeechEvent("Audio Chunk"), null);
    }

    eventHandler.onEvent(new TextToSpeechEvent("Complete"), null);
    client.close();

    return new MediaSession(sessionId, eventHandler, new MediaStream() {
        @Override
        public void write(byte[] data) {
        }

        @Override
        public void close() {
        }
    });
} catch (Exception e) {
    throw new RuntimeException("Exception in startTextToSpeechSession", e);
}
}}
