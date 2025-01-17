package com.gintel.cognitiveservices.tts.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import java.io.IOException;

import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.TextToSpeechEvent;
import com.gintel.cognitiveservices.core.tts.types.InputFormat;
import com.gintel.cognitiveservices.core.tts.types.OutputFormatCore;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechByteResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechStatus;
import com.google.api.gax.core.FixedCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleTextToSpeechService implements TextToSpeech {
    private static final Logger logger = LoggerFactory.getLogger(GoogleTextToSpeechService.class);

    private TextToSpeechClient client;

    public String getProvider() {
        return "google";
    }

    public GoogleTextToSpeechService() {
        try {
            client = TextToSpeechClient.create();
        } catch (IOException e) {
            logger.error("Failed to initialize GoogleTextToSpeechService", e);
            throw new RuntimeException("Failed to initialize GoogleTextToSpeechService", e);
        }
    }

    // Initiates speech from text in undefined audio-byte format
    @Override
    public TextToSpeechResult textToSpeech(String language, String voiceName, String text, InputFormat input,
            OutputFormatCore output) {
        return synthesizeTextToByteArray(language, voiceName, text, null);
    }

    // Initiates speech from text as Mp3 data in byte[] format
    @Override
    public TextToSpeechByteResult textToStream(String language, String voiceName, String text, InputFormat input,
            OutputFormatCore output, MediaStream outputStream) {
        return synthesizeTextToStream(language, voiceName, text, null, outputStream);
    }

    // Generates speech from text in undefined audio-byte format
    private TextToSpeechResult synthesizeTextToByteArray(String languageCode, String voiceName, String text,
            AudioEncoding audioEncoding) {
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

    // Generates speech from text as Mp3 data in byte[] format
    private TextToSpeechByteResult synthesizeTextToStream(String languageCode, String voiceName, String text,
            AudioEncoding audioEncoding, MediaStream outputStream) {

        try {
            TextToSpeechClient ttsClient = TextToSpeechClient.create();
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setName(voiceName)
                    .build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            SynthesizeSpeechResponse response = ttsClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();
            byte[] buffer = audioContents.toByteArray();

            return new TextToSpeechByteResult(TextToSpeechStatus.OK, buffer);
        } catch (Exception ex) {
            logger.error("Exception in textToSpeech", ex);
        }

        return new TextToSpeechByteResult(TextToSpeechStatus.ERROR, null);
    }

    // Starts a text-to-speech session and returns a MediaSession object.
    // Unfinished implementation
    @Override
    public MediaSession startTextToSpeechSession(String sessionId, String text, String language,
            EventHandler<BaseEvent> eventHandler) {

        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");

            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();
            TextToSpeechClient client = TextToSpeechClient.create(settings);

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(language)
                    .setName("en-US-Standard-A")
                    .build();
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.LINEAR16) // Adjust encoding as needed
                    .build();

            SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioContents = response.getAudioContent();

            int chunkSize = 1024; // Adjust chunk size as needed
            for (int i = 0; i < audioContents.size(); i += chunkSize) {
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

                @Override
                public void write(String data) {
                    throw new UnsupportedOperationException("Unimplemented method 'write' for String input");
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Exception in startTextToSpeechSession", e);
        }
    }
}
