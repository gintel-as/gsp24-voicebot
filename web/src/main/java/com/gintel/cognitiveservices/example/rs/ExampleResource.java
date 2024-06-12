package com.gintel.cognitiveservices.example.rs;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.example.ExampleController;

public class ExampleResource implements Example {
    private static final Logger logger = LoggerFactory.getLogger(ExampleResource.class);

    private ExampleController controller;

    public ExampleResource(ExampleController controller) {
        this.controller = controller;
    }

    @Override
    public Response echo(String language, String voiceName, String text) throws Exception {
        logger.info("echo({}, {}, {})", language, voiceName, text);

        return Response.ok().entity(controller.textToSpeech(language, voiceName, text)).build();
    }
}
