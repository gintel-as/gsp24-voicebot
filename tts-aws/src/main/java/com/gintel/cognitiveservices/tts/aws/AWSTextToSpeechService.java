package com.gintel.cognitiveservices.tts.aws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.VoiceId;
import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.types.InputFormat;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechByteResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechStatus;

import com.gintel.cognitiveservices.core.tts.types.OutputFormatCore;

public class AWSTextToSpeechService implements TextToSpeech {
    private static final Logger logger = LoggerFactory.getLogger(AWSTextToSpeechService.class);
    private AmazonPolly client;

    @Override
    public String getProvider() {
        return "aws";
    }

    // Constructor to initialize the AWS Polly client
    public AWSTextToSpeechService(AWSTTSConfig serviceConfig) {
        try {
            client = AmazonPollyClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1)
                    .withCredentials(new ProfileCredentialsProvider(serviceConfig.userName()))
                    .build();
        } catch (Exception e) {
            logger.error("Failed to initialize AWSTextToSpeechService", e);
            throw new RuntimeException("Failed to initialize AWSTextToSpeechService", e);
        }
    }

    @Override
    public TextToSpeechResult textToSpeech(String language, String voiceName, String text, InputFormat input,
            OutputFormatCore output) {
        return synthesizeTextToByteArray(language, voiceName, text, null);
    }

    @Override
    public TextToSpeechByteResult textToStream(String language, String voiceName, String text, InputFormat input,
            OutputFormatCore output, MediaStream outputStream) {
        return synthesizeTextToStream(language, voiceName, text, null, outputStream);
    }

    private TextToSpeechResult synthesizeTextToByteArray(String languageCode, String voiceName, String text,
            OutputFormatCore outputFormat) {
        // Creates a request to synthesize speech with the given parameters
        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
                .withText(text)
                .withVoiceId(VoiceId.fromValue(voiceName))
                .withOutputFormat(OutputFormat.Mp3);

        // Sends the request to AWS Polly and gets the result
        SynthesizeSpeechResult synthRes = client.synthesizeSpeech(synthReq);
        InputStream audioStream = synthRes.getAudioStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        try {
            // Reads the audio stream and writes it to a byte array output stream
            while ((read = audioStream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            byte[] audioData = baos.toByteArray();
            // Trims the audio data to the first 1000 bytes if needed
            byte[] trimmedAudioData = new byte[Math.min(1000, audioData.length)];
            System.arraycopy(audioData, 0, trimmedAudioData, 0, trimmedAudioData.length);
            return new TextToSpeechResult(TextToSpeechStatus.OK, null, null);
        } catch (IOException e) {
            logger.error("Exception in synthesizeTextToByteArray", e);
        }

        return new TextToSpeechResult(TextToSpeechStatus.ERROR, null, null);
    }

    private TextToSpeechByteResult synthesizeTextToStream(String language, String voiceName, String text,
            OutputFormatCore output, MediaStream outputStream) {

        logger.info(voiceName);
        // Creates a request to synthesize speech with the given parameters
        SynthesizeSpeechRequest synthesizeSpeechRequest = new SynthesizeSpeechRequest()
                .withOutputFormat(OutputFormat.Mp3)
                .withVoiceId(VoiceId.fromValue(voiceName))
                .withText(text);

        // Sends the request to AWS Polly and gets the result
        SynthesizeSpeechResult synthRes = client.synthesizeSpeech(synthesizeSpeechRequest);
        InputStream audioStream = synthRes.getAudioStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        try {
            // Reads the audio stream and writes it to a byte array output stream
            while ((read = audioStream.read(buffer)) > 0) {
                baos.write(buffer, 0, read);
            }
            byte[] audioData = baos.toByteArray();
            if (outputStream != null) {
                outputStream.write(audioData);
            }
            return new TextToSpeechByteResult(TextToSpeechStatus.OK, audioData);
        } catch (IOException e) {
            logger.error("Exception in synthesizeTextToByteArray", e);
        }
        return new TextToSpeechByteResult(TextToSpeechStatus.ERROR, null);
    }

    @Override
    public MediaSession startTextToSpeechSession(String sessionId, String text, String language,
            EventHandler<BaseEvent> handler) {
        return null;
    }
}
