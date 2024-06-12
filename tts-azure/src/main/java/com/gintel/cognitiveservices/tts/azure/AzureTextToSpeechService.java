package com.gintel.cognitiveservices.tts.azure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
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

    public AzureTextToSpeechService() {
        serviceConfig = ConfigFactory.create(AzureTTSConfig.class);
    }

    @Override
    public TextToSpeechResult textToSpeech(String language, String voiceName, String text,
            InputFormat input, OutputFormat output) {

        SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(), serviceConfig.region());
        //config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio24Khz96KBitRateMonoMp3);

        //AudioConfig audioConfig = AudioConfig.fromWavFileOutput("output/output.mp3");
        List<SpeechSynthesisWordBoundaryEventArgs> wordBoundaries = new ArrayList<>();

        try (SpeechSynthesizer synthesizer = new SpeechSynthesizer(config)) {
            // Subscribes to word boundary event
            synthesizer.WordBoundary.addEventListener((o, e) -> {
                // The unit of e.AudioOffset is tick (1 tick = 100 nanoseconds), divide by 10,000 to convert to milliseconds.
                System.out.print("Word boundary event received. Audio offset: " + (e.getAudioOffset() + 5000) / 10000 + "ms, ");
                System.out.println("text offset: " + e.getTextOffset() + ", word length: " + e.getWordLength() + ".");
                wordBoundaries.add(e);
            });

            try (SpeechSynthesisResult result = synthesizer.SpeakSsmlAsync(text).get()) {
                if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                    byte[] audioData = result.getAudioData();

                    logger.debug("Speech synthesized. {} bytes of audio data received.", audioData.length);

                    String srt = getSrt(text, wordBoundaries);

                    return new TextToSpeechResult(TextToSpeechStatus.OK, audioData, srt);
                }
                else if (result.getReason() == ResultReason.Canceled) {
                    SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
                    System.out.println("CANCELED: Reason=" + cancellation.getReason());

                    if (cancellation.getReason() == CancellationReason.Error) {
                        System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                        System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                        System.out.println("CANCELED: Did you update the subscription info?");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in textToSpeech", ex);
        }
        return new TextToSpeechResult(TextToSpeechStatus.ERROR, null, null);
    }

    private String getSrt(String text, List<SpeechSynthesisWordBoundaryEventArgs> wordBoundaries) {
        StringBuilder srtBuilder2 = new StringBuilder();

        for (int idx = 0; idx < wordBoundaries.size(); idx++) {
            srtBuilder2.append(Integer.toString(idx + 1) + '\n');
            SpeechSynthesisWordBoundaryEventArgs e = wordBoundaries.get(idx);
            long millis = e.getAudioOffset() / 10000;
            String startTime = String.format("%02d:%02d:%02d,%03d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                    millis % 1000);
            long endMillis;
            if (idx < wordBoundaries.size() - 1) {
                endMillis = wordBoundaries.get(idx + 1).getAudioOffset() / 10000;
            } else {
                endMillis = millis + 1000;
            }
            String endTime = String.format("%02d:%02d:%02d,%03d", TimeUnit.MILLISECONDS.toHours(endMillis),
                    TimeUnit.MILLISECONDS.toMinutes(endMillis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(endMillis) % TimeUnit.MINUTES.toSeconds(1),
                    endMillis % 1000);
            srtBuilder2.append(startTime + " --> " + endTime + '\n');
            srtBuilder2.append(text.substring((int)e.getTextOffset(), (int)(e.getTextOffset() + e.getWordLength())) + "\n\n");
        }
        return srtBuilder2.toString();
    }
}
