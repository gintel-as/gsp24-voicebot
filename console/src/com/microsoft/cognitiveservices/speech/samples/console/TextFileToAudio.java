package com.microsoft.cognitiveservices.speech.samples.console;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisWordBoundaryEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

public class TextFileToAudio {
     @SuppressWarnings("resource")
    public static void textToAudio() throws InterruptedException, ExecutionException, IOException {
        // Creates an instance of a speech config with specified
        // subscription key and service region. Replace with your own subscription key
        // and service region (e.g., "westus").
        // The default language is "en-us".
        SpeechConfig config = SpeechConfig.fromSubscription("b16f6e70cac14487af395758c3db4e59", "norwayeast");
        //config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio24Khz96KBitRateMonoMp3);
        
        // Save to audio file
        //AudioConfig audioConfig = AudioConfig.fromWavFileOutput("output/output.mp3");
        List<SpeechSynthesisWordBoundaryEventArgs> wordBoundaries = new ArrayList<>();

        SpeechSynthesizer synthesizer = new SpeechSynthesizer(config);
        {
            // Subscribes to word boundary event
            synthesizer.WordBoundary.addEventListener((o, e) -> {
                // The unit of e.AudioOffset is tick (1 tick = 100 nanoseconds), divide by 10,000 to convert to milliseconds.
                System.out.print("Word boundary event received. Audio offset: " + (e.getAudioOffset() + 5000) / 10000 + "ms, ");
                System.out.println("text offset: " + e.getTextOffset() + ", word length: " + e.getWordLength() + ".");
                wordBoundaries.add(e);
            });

            String ssml;
            try {
                BufferedReader reader = new BufferedReader(new FileReader("input/ssml.txt"));
                ssml = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            SpeechSynthesisResult result = synthesizer.SpeakSsmlAsync(ssml).get();

            // Checks result.
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                System.out.println("Speech synthesized.");
                byte[] audioData = result.getAudioData();
                System.out.println(audioData.length + " bytes of audio data received.");

                // Save to SRT
                FileWriter writer = new FileWriter("output/output.srt");

                for (int idx = 0; idx < wordBoundaries.size(); idx++) {
                    writer.write(Integer.toString(idx + 1) + '\n');
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
                    writer.write(startTime + " --> " + endTime + '\n');
                    writer.write(ssml.substring((int)e.getTextOffset(), (int)(e.getTextOffset() + e.getWordLength())) + "\n\n");
                }
                writer.close();
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
            result.close();
        }
        synthesizer.close();
    }
    
    
}
