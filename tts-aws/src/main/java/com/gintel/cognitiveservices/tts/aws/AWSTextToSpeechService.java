package com.gintel.cognitiveservices.tts.aws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.partitions.model.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;
import com.gintel.cognitiveservices.core.communication.EventHandler;
import com.gintel.cognitiveservices.core.communication.MediaStream;
import com.gintel.cognitiveservices.core.communication.types.BaseEvent;
import com.gintel.cognitiveservices.core.communication.types.MediaSession;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.TextToSpeechEvent;
import com.gintel.cognitiveservices.core.tts.types.InputFormat;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechByteResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechStatus;
import com.gintel.cognitiveservices.core.tts.types.OutputFormatCore;

public class AWSTextToSpeechService implements TextToSpeech {
    private static final Logger logger = LoggerFactory.getLogger(AWSTextToSpeechService.class);
    private AmazonPolly polly;
    private Voice voice;

    @Override
    public String getProvider() {

        return "aws";
    }

    public AWSTextToSpeechService() {
        // Region region
        try {
            polly = AmazonPollyClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_3)
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .build();

            DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
            DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
            voice = describeVoicesResult.getVoices().get(0);
        } catch (Exception e) {
            logger.error("Failed to initialize AWSTextToSpeechService", e);
            throw new RuntimeException("Failed to initialize AWSTextToSpeechService", e);
        }

    }

    @Override
    public TextToSpeechResult textToSpeech(String language, String voiceName, String text, InputFormat input,
            OutputFormatCore output) {

        return synthesizeTextToByteArray(language, voiceName, text, OutputFormat.Mp3);
    }

    @Override
    public TextToSpeechByteResult textToStream(String language, String voiceName, String text, InputFormat input,
            OutputFormatCore output, MediaStream outputStream) {
        return synthesizeTextToStream(language, voiceName, text, OutputFormat.Mp3, outputStream);
    }

    private TextToSpeechResult synthesizeTextToByteArray(String languageCode, String voiceName, String text,
            OutputFormat outputFormat) {
        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
                .withText(text)
                .withVoiceId(voiceName)
                .withOutputFormat(outputFormat);

        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream audioStream = synthRes.getAudioStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        try {
            while ((read = audioStream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);

            }
            byte[] audioData = baos.toByteArray();
            byte[] trimmedAudioData = new byte[Math.min(1000, audioData.length)];
            System.arraycopy(audioData, 0, trimmedAudioData, 0, trimmedAudioData.length);
            return new TextToSpeechResult(TextToSpeechStatus.OK, null, null);
        } catch (IOException e) {
            logger.error("Exception in synthesizeTextToByteArray", e);
        }

        return new TextToSpeechResult(TextToSpeechStatus.ERROR, null, null);
    }

    private TextToSpeechByteResult synthesizeTextToStream(String language, String voiceName, String text,
            OutputFormat output, MediaStream outputStream) {

        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
                .withText(text)
                .withVoiceId(voiceName)
                .withOutputFormat(output);
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream audioStream = synthRes.getAudioStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        try {
            while ((read = audioStream.read(buffer)) != -1) {
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
        logger.info("createSession(sessionId={}, language={})", sessionId, language);

        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
                .withText(text)
                .withVoiceId(voice.getId())
                .withOutputFormat(OutputFormat.Mp3).withEngine("neural");
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
        InputStream audioStream = synthRes.getAudioStream();

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int read;
            while ((read = audioStream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            byte[] audioData = baos.toByteArray();
            int chunkSize = 1024;
            for (int i = 0; i < audioData.length; i += chunkSize) {
                byte[] chunk = new byte[Math.min(chunkSize, audioData.length - i)];
                System.arraycopy(audioData, i, chunk, 0, chunk.length);
                handler.onEvent(new TextToSpeechEvent("Audio Chunk"), null);
            }

            handler.onEvent(new TextToSpeechEvent("Complete"), null);

            return new MediaSession(sessionId, handler, new MediaStream() {
                @Override
                public void write(byte[] data) {
                }

                @Override
                public void close() {
                }

                @Override
                public void write(String data) {
                    throw new UnsupportedOperationException("Unimplemented method 'write'");
                }
            });

        } catch (IOException e) {
            throw new RuntimeException("Exception in startTextToSpeechSession", e);
        }

    }

}