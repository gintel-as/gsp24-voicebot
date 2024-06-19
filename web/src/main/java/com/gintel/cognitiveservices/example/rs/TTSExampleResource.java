package com.gintel.cognitiveservices.example.rs;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.example.ExampleController;

public class TTSExampleResource implements TTSExample {
    private static final Logger logger = LoggerFactory.getLogger(TTSExampleResource.class);

    private ExampleController controller;

    public TTSExampleResource(ExampleController controller) {
        this.controller = controller;
    }

    @Override
    public Response echo(String language, String voiceName, String text) throws Exception {
        logger.info("echo({}, {}, {})", language, voiceName, text);

        return Response.ok().entity(controller.textToSpeech(language, voiceName, text)).build();
    }
}
