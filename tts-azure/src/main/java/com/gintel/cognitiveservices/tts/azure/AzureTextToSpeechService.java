package com.gintel.cognitiveservices.tts.azure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.core.tts.types.InputFormat;
import com.gintel.cognitiveservices.core.tts.types.OutputFormat;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechStatus;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisWordBoundaryEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

public class AzureTextToSpeechService implements TextToSpeech {
    private static final Logger logger = LoggerFactory.getLogger(AzureTextToSpeechService.class);

    private AzureTTSConfig serviceConfig;

    public AzureTextToSpeechService(AzureTTSConfig serviceConfig) {
        this.serviceConfig = serviceConfig;

        logger.info("region is {}", serviceConfig.region());
    }

    @Override
    public TextToSpeechResult textToSpeech(String language, String voiceName, String text,
            InputFormat input, OutputFormat output) {

        SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(), serviceConfig.region());
        if (voiceName != null) {
            config.setSpeechSynthesisVoiceName(voiceName);
        }
        List<SpeechSynthesisWordBoundaryEventArgs> wordBoundaries = new ArrayList<>();

        try (SpeechSynthesizer synth = new SpeechSynthesizer(config)) {

            assert (config != null);
            assert (synth != null);

            synth.WordBoundary.addEventListener((s, e) -> {
                wordBoundaries.add(e);
            });

            int exitCode = 1;

            Future<SpeechSynthesisResult> task = synth.SpeakTextAsync(text);
            assert (task != null);

            SpeechSynthesisResult result = task.get();
            assert (result != null);

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                String srt = generateSrt(wordBoundaries, text);
                byte[] audioData = result.getAudioData(); // Get the audio data
                byte[] trimmedAudioData = new byte[Math.min(1000, audioData.length)]; // Trim to first 100 bytes
                System.arraycopy(audioData, 0, trimmedAudioData, 0, trimmedAudioData.length);
                exitCode = 0;
                return new TextToSpeechResult(TextToSpeechStatus.OK, trimmedAudioData, srt);
            } else if (result.getReason() == ResultReason.Canceled) {
                SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails
                        .fromResult(result);
                System.out.println("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    System.out.println("CANCELED: Did you update the subscription info?");
                }
            }

            System.exit(exitCode);
        } catch (Exception ex) {
            logger.error("Exception in textToSpeech", ex);
        }
        return new TextToSpeechResult(TextToSpeechStatus.ERROR, null, null);
    }

    private String generateSrt(List<SpeechSynthesisWordBoundaryEventArgs> wordBoundaries, String text) {
        StringBuilder srt = new StringBuilder();
        int counter = 1;
        long startTime = 0;
        long endTime = 0;

        for (SpeechSynthesisWordBoundaryEventArgs boundary : wordBoundaries) {
            endTime = boundary.getAudioOffset() / 10000; // Convert from 100-nanoseconds to milliseconds

            srt.append(counter)
                .append("\n")
                .append(formatTime(startTime))
                .append(" --> ")
                .append(formatTime(endTime))
                .append("\n")
                .append(text.substring((int) boundary.getTextOffset(), (int) (boundary.getTextOffset() + boundary.getWordLength())))
                .append("\n\n");

            startTime = endTime;
            counter++;
        }

        return srt.toString();
    }

    private String formatTime(long timeInMilliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(timeInMilliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) % 60;
        long milliseconds = timeInMilliseconds % 1000;

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
    }
}
