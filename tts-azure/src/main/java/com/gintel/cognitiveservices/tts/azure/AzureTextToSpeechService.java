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
import com.gintel.cognitiveservices.core.tts.types.OutputFormatCore;
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

    public String getProvider() {
        return "azure";
    }

    public AzureTextToSpeechService(AzureTTSConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        logger.info("region is {}", serviceConfig.region());
    }

    // Generates speech from text in undefined audio-byte format
    @Override
    public TextToSpeechResult textToSpeech(String language, String voiceName, String text,
            InputFormat input, OutputFormatCore output) {
        // Creates a SpeechConfig with subscription key and region
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

            // Asynchronously synthesizes the text to speech
            Future<SpeechSynthesisResult> task = synth.SpeakTextAsync(text);
            assert (task != null);

            SpeechSynthesisResult result = task.get();
            assert (result != null);

            // Trims and returns data, and handles result errors
            if (result.getReason() == ResultReason.SynthesizingAudioCompleted) {
                String srt = generateSrt(wordBoundaries, text);
                byte[] audioData = result.getAudioData();
                byte[] trimmedAudioData = new byte[Math.min(1000, audioData.length)];
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

    // Initites SpeechConfig and handle output stream configuration
    @Override
    public TextToSpeechByteResult textToStream(String language, String voiceName, String text,
            InputFormat input, OutputFormatCore output, MediaStream outputStream) {
        // Creates a SpeechConfig with subscription key and region
        SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(),
                serviceConfig.region());
        if (voiceName != null) {
            config.setSpeechSynthesisVoiceName(voiceName);
        }

        config.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Audio16Khz32KBitRateMonoMp3);

        if (outputStream != null) {
            // If an output stream is provided, process asynchronously
            TaskExecutorService.getInstance().submit(() -> doAsync(config, outputStream, text));
        } else {
            // Otherwise, process synchronously
            return doSynchronous(config, text);
        }

        return new TextToSpeechByteResult(TextToSpeechStatus.ERROR, null);
    }

    // Generates speech from text synchronously and returns result as one chunck of
    // Mp3 data in byte[] format
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

    // Generates speech from text asynchronously and return stream of Mp3 audio
    private void doAsync(SpeechConfig config, MediaStream outputStream, String text) {
        logger.info("doAsync: text = {}", text);

        List<SpeechSynthesisWordBoundaryEventArgs> wordBoundaries = new ArrayList<>();

        try (SpeechSynthesizer synth = new SpeechSynthesizer(config, null)) {
            synth.WordBoundary.addEventListener((s, e) -> {
                wordBoundaries.add(e);
            });

            // Starts synthesizing the text to speech
            SpeechSynthesisResult result = synth.StartSpeakingText(text);
            try (AudioDataStream audioDataStream = AudioDataStream.fromResult(result)
            // FileOutputStream for testing - uncomment to listen to audio via the
            // referenced file
            // (also uncomment fos.write below)
            // ;FileOutputStream fos = new FileOutputStream(new
            // File("c:\\temp\\voicegw\\test2.wav"))
            ) {
                byte[] buffer = new byte[1600];

                while (true) {
                    long len = audioDataStream.readData(buffer);
                    if (len == 0) {
                        break;
                    }

                    byte[] chunk = new byte[(int) len];
                    System.arraycopy(buffer, 0, chunk, 0, (int) len);
                    // fos.write(buffer, 0, (int) len);

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
            // Implement: notifying client
        }
    }

    // Generates SRT (SubRip Subtitle) format from word boundaries and text
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
                    .append(text.substring((int) boundary.getTextOffset(),
                            (int) (boundary.getTextOffset() + boundary.getWordLength())))
                    .append("\n\n");

            startTime = endTime;
            counter++;
        }

        return srt.toString();
    }

    // Formats time in milliseconds to HH:mm:ss,SSS format
    private String formatTime(long timeInMilliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(timeInMilliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMilliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMilliseconds) % 60;
        long milliseconds = timeInMilliseconds % 1000;

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
    }

    // Starts a text-to-speech session and returns a MediaSession object
    public MediaSession startTextToSpeechSession(String sessionId, String text, String language,
            EventHandler<BaseEvent> eventHandler) {
        logger.info("createSession(sessionId={}, language={})", sessionId, language);
        String serviceRegion = serviceConfig.region();

        try {
            SpeechConfig config = SpeechConfig.fromSubscription(serviceConfig.subscriptionKey(), serviceRegion);
            PushAudioOutputStream os = AudioOutputStream.createPushStream(null);
            AudioConfig audioConfig = AudioConfig.fromStreamOutput(os);
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(config, audioConfig);

            synthesizer.SynthesisStarted.addEventListener((s, e) -> eventHandler.onEvent(s, new TextToSpeechEvent("")));
            synthesizer.SynthesisCompleted
                    .addEventListener((s, e) -> eventHandler.onEvent(s, new TextToSpeechEvent("")));
            synthesizer.SynthesisCanceled.addEventListener((s, e) -> {
                String result = "CANCELED";
                eventHandler.onEvent(s, new TextToSpeechEvent(result));
            });
            synthesizer.SpeakTextAsync(text);

            return new MediaSession(sessionId, eventHandler, new MediaStream() {
                @Override
                public void write(byte[] data) {
                }

                @Override
                public void close() {
                    os.close();
                    synthesizer.close();
                    config.close();
                }

                @Override
                public void write(String data) {
                    throw new UnsupportedOperationException("Unimplemented method 'write' for String input");
                }

            });
        } catch (Exception e) {
            throw new RuntimeException("Exception in textToSpeechSession", e);
        }
    }

}
