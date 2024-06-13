package com.microsoft.cognitiveservices.speech.samples.console;

import java.io.FileWriter;
import java.util.concurrent.Future;

import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

public class WavAudioTotext {
    /**
     * @param args Arguments are ignored in this sample.
     */
    public static void audioToText(String[] args) {

        // Replace below with your own subscription key
        String speechSubscriptionKey = "b16f6e70cac14487af395758c3db4e59";
        // Replace below with your own service region (e.g., "westus").
        String serviceRegion = "norwayeast";
        // Specify the path to your .wav file
        String audioFilePath = "path/to/your/file.wav";

        // Creates an instance of a speech recognizer using speech configuration with specified
        // subscription key and service region and audio file as input.
        try (SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
             AudioConfig audioInput = AudioConfig.fromWavFileInput(audioFilePath);
             SpeechRecognizer reco = new SpeechRecognizer(config, "nb-NO", audioInput)) {

            assert(config != null);
            assert(reco != null);
            int exitCode = 1;

            System.out.println("Recognizing from audio file...");

            Future<com.microsoft.cognitiveservices.speech.SpeechRecognitionResult> task = reco.recognizeOnceAsync();
            assert(task != null);

            com.microsoft.cognitiveservices.speech.SpeechRecognitionResult result = task.get();
            assert(result != null);

            if (result.getReason() == ResultReason.RecognizedSpeech) {
                System.out.println("We recognized: " + result.getText());
                FileWriter writer = new FileWriter("output/output.txt");
                writer.write(result.getText());
                writer.close();
                exitCode = 0;
            } else if (result.getReason() == ResultReason.NoMatch) {
                System.out.println("NOMATCH: Speech could not be recognized.");
            } else if (result.getReason() == ResultReason.Canceled) {
                CancellationDetails cancellation = CancellationDetails.fromResult(result);
                System.out.println("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    System.out.println("CANCELED: Did you update the subscription info?");
                }
            }
            
            System.exit(exitCode);
        } catch (Exception ex) {
            System.out.println("Unexpected exception: " + ex.getMessage());

            assert(false);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        audioToText(args);
    }
}
