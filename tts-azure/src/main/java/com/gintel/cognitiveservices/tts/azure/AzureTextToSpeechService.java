package com.gintel.cognitiveservices.tts.azure;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.gintel.cognitiveservices.internal.TaskExecutorService;
import com.microsoft.cognitiveservices.speech.AudioDataStream;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisWordBoundaryEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.StreamStatus;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PushAudioOutputStream;

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

            Future<SpeechSynthesisResult> task = synth.SpeakTextAsync(text);
            assert (task != null);

            SpeechSynthesisResult result = task.get();
            assert (result != null);

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                String srt = generateSrt(wordBoundaries, text);
                byte[] audioData = result.getAudioData(); // Get the audio data
                byte[] trimmedAudioData = new byte[Math.min(1000, audioData.length)]; // Trim to first 100 bytes
                System.arraycopy(audioData, 0, trimmedAudioData, 0, trimmedAudioData.length);
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
        } catch (Exception ex) {
            logger.error("Exception in textToSpeech", ex);
        }
        return new TextToSpeechResult(TextToSpeechStatus.ERROR, null, null);
    }

    @Override
    public TextToSpeechByteResult textToStream(String language, String voiceName, String text,
            InputFormat input, OutputFormat output, MediaStream outputStream) {

        SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(),
                serviceConfig.region());
        if (voiceName != null) {
            config.setSpeechSynthesisVoiceName(voiceName);
        }

        config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);

        if (outputStream != null) {
            TaskExecutorService.getInstance().submit(() -> doAsync(config, outputStream, text));
        } else {
            return doSynchronous(config, text);
        }

        return new TextToSpeechByteResult(TextToSpeechStatus.ERROR, null);
    }

    private TextToSpeechByteResult doSynchronous(SpeechConfig config, String text) {
        List<SpeechSynthesisWordBoundaryEventArgs> wordBoundaries = new ArrayList<>();

        try (SpeechSynthesizer synth = new SpeechSynthesizer(config, null)) {
            synth.WordBoundary.addEventListener((s, e) -> {
                wordBoundaries.add(e);
            });

            Future<SpeechSynthesisResult> task = synth.SpeakTextAsync(text);
            assert (task != null);

            SpeechSynthesisResult result = task.get();
            assert (result != null);

            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                byte[] audioData = result.getAudioData();
                return new TextToSpeechByteResult(TextToSpeechStatus.OK, audioData);
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
        } catch (Exception ex) {
            logger.error("Exception in doSynchronous", ex);
        }
        return new TextToSpeechByteResult(TextToSpeechStatus.ERROR, null);
    }

    private void doAsync(SpeechConfig config, MediaStream outputStream, String text) {
        logger.info("doAsync: text = {}", text);

        List<SpeechSynthesisWordBoundaryEventArgs> wordBoundaries = new ArrayList<>();

        try (SpeechSynthesizer synth = new SpeechSynthesizer(config, null)) {
            synth.WordBoundary.addEventListener((s, e) -> {
                wordBoundaries.add(e);
            });

            SpeechSynthesisResult result = synth.StartSpeakingText(text);
            try (AudioDataStream audioDataStream = AudioDataStream.fromResult(result)
//                    FileOutputStream for testing - uncomment to listen to audio via the referenced file
//                    (also uncomment fos.write below)
//                    ;FileOutputStream fos = new FileOutputStream(new File("c:\\temp\\voicegw\\test2.wav"))  
                    ) {
                byte[] buffer = new byte[1600];

                while (true) {
                    long len = audioDataStream.readData(buffer);
                    if (len == 0) {
                        break;
                    }

                    byte[] chunk = new byte[(int) len];
                    System.arraycopy(buffer, 0, chunk, 0, (int) len);
//                    fos.write(buffer, 0, (int) len);

                    outputStream.write(chunk);
                }

                if (audioDataStream.getStatus() != StreamStatus.AllData) {
                    SpeechSynthesisCancellationDetails speechSynthesisCancellationDetails = SpeechSynthesisCancellationDetails
                            .fromStream(audioDataStream);
                    logger.warn("Did not receive all data - cancellation details {}",
                            speechSynthesisCancellationDetails);
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in doAsync", ex);
            // TODO notify client
        }
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
    public MediaSession startTextToSpeechSession(String sessionId, String text, String language, EventHandler<BaseEvent> eventHandler) {
        logger.info("createSession(sessionId={}, language={})", sessionId, language);
        String serviceRegion = serviceConfig.region();
        // String lang = language != null ? language.replace("\"", "") : "nb-NO";

        try {
            SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(), serviceRegion);
            PushAudioOutputStream os = AudioOutputStream.createPushStream(null);
            AudioConfig audioConfig = AudioConfig.fromStreamOutput(os);
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(config, audioConfig);

            synthesizer.SynthesisStarted.addEventListener((s, e) -> eventHandler.onEvent(s, new TextToSpeechEvent("")));
            synthesizer.SynthesisCompleted.addEventListener((s, e) -> eventHandler.onEvent(s, new TextToSpeechEvent("")));
            synthesizer.SynthesisCanceled.addEventListener((s, e) -> {
                String result = "CANCELED";
                eventHandler.onEvent(s, new TextToSpeechEvent(result));
            });
            synthesizer.SpeakTextAsync(text);

            return new MediaSession(sessionId, eventHandler, new MediaStream() {
                @Override
                public void write(byte[] data) {}

                @Override
                public void close() {
                    os.close();
                    synthesizer.close();
                    config.close();
                }

            });
        } catch (Exception e) {
            throw new RuntimeException("Exception in textToSpeechSession", e);
        }
    }




}
