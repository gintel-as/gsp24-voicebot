package com.gintel.cognitiveservices.rs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.config.WebConfig;
import com.gintel.cognitiveservices.core.openai.Openai;
import com.gintel.cognitiveservices.core.stt.SpeechToText;
import com.gintel.cognitiveservices.core.tts.TextToSpeech;
import com.gintel.cognitiveservices.example.ExampleController;
import com.gintel.cognitiveservices.example.rs.TTSExampleResource;
import com.gintel.cognitiveservices.example.rs.STTExampleResource;
import com.gintel.cognitiveservices.example.rs.OpenaiExampleResource;
import com.gintel.cognitiveservices.rs.filters.LogRequestFilter;
import com.gintel.cognitiveservices.stt.azure.AzureSTTConfig;
import com.gintel.cognitiveservices.stt.azure.AzureSpeechToTextService;
import com.gintel.cognitiveservices.tts.azure.AzureTTSConfig;
import com.gintel.cognitiveservices.tts.azure.AzureTextToSpeechService;
import com.gintel.cognitiveservices.openai.azure.AzureOpenaiConfig;
import com.gintel.cognitiveservices.openai.azure.AzureOpenaiService;

@ApplicationPath("/")
public class WebApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(WebApplication.class);

    @Context
    private ServletContext servletContext;

    public WebApplication() {
        logger.info("Starting rest services");
    }

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> objects = new HashSet<>();
        objects.add(GenericExceptionHandler.class);
        return objects;
    }

    @Override
    public Set<Object> getSingletons() {
        String strPath = System.getProperty("catalina.base");
        ConfigFactory.setProperty("config_file", strPath + "/conf/web.properties");
        final WebConfig config = ConfigFactory.create(WebConfig.class);

        List<TextToSpeech> ttsServices = getTextToSpeechServices();
        List<SpeechToText> sttServices = getSpeechToTextServices();
        List<Openai> openaiServices = getOpenaiServices();
        final TTSExampleResource authResource = new TTSExampleResource(new ExampleController(config, ttsServices, sttServices, openaiServices));
        final STTExampleResource authSTTResource = new STTExampleResource(new ExampleController(config, ttsServices, sttServices, openaiServices));
        final OpenaiExampleResource authOpenaiResource = new OpenaiExampleResource(new ExampleController(config, ttsServices, sttServices, openaiServices));
        final LogRequestFilter logRequestFilter = new LogRequestFilter();

        Set<Object> singletons = new HashSet<>();
        singletons.add(authResource);
        singletons.add(authSTTResource);
        singletons.add(authOpenaiResource);
        singletons.add(logRequestFilter);
        return singletons;
    }

    private List<SpeechToText> getSpeechToTextServices() {
        // TODO Auto-generated method stub
        return Arrays.asList(new AzureSpeechToTextService(ConfigFactory.create(AzureSTTConfig.class)));
    }

    private List<TextToSpeech> getTextToSpeechServices() {
        return Arrays.asList(new AzureTextToSpeechService(ConfigFactory.create(AzureTTSConfig.class)));
    }

    private List<Openai> getOpenaiServices() {
        return Arrays.asList(new AzureOpenaiService(ConfigFactory.create(AzureOpenaiConfig.class)));
    }
}
