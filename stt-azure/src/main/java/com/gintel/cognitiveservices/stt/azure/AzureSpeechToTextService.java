package com.gintel.cognitiveservices.stt.azure;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.stt.SpeechToText;
import com.gintel.cognitiveservices.core.stt.types.InputFormat;
import com.gintel.cognitiveservices.core.stt.types.OutputFormat;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextResult;
import com.gintel.cognitiveservices.core.stt.types.SpeechToTextStatus;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechResult;
import com.gintel.cognitiveservices.core.tts.types.TextToSpeechStatus;
import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisWordBoundaryEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;

public class AzureSpeechToTextService implements SpeechToText{
    private static final Logger logger = LoggerFactory.getLogger(AzureSpeechToTextService.class);

    private AzureSTTConfig serviceConfig;

    public AzureSpeechToTextService(AzureSTTConfig serviceConfig) {
        this.serviceConfig = serviceConfig;

        logger.info("region is {}", serviceConfig.region());
    }

    @Override
    public SpeechToTextResult speechToText(String language, String voiceName, String text,
            InputFormat input, OutputFormat output) {
        
        String serviceRegion = serviceConfig.region();


        //SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(), serviceConfig.region());
        //config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio24Khz96KBitRateMonoMp3);
        //if (voiceName != null) {
        //    config.setSpeechSynthesisVoiceName(voiceName);
        //}
        //AudioConfig audioConfig = AudioConfig.fromWavFileOutput("output/output.mp3");
       // SpeechRecognizer reco = new SpeechRecognizer(config, "nb-NO");
       List<SpeechRecognizer> recognizeSpeechRecognizers = new ArrayList<>();

        try (SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(), serviceRegion);
             SpeechRecognizer reco = new SpeechRecognizer(config, "nb-NO")) {

            assert(config != null);
            assert(reco != null);
            int exitCode = 1;

            System.out.println("Say something...");

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
            }
            else if (result.getReason() == ResultReason.NoMatch) {
                System.out.println("NOMATCH: Speech could not be recognized.");
            }
            else if (result.getReason() == ResultReason.Canceled) {
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
            logger.error("Exception in speechToText", ex);
        }
        return new SpeechToTextResult(SpeechToTextStatus.ERROR, null, null);
    }
}
