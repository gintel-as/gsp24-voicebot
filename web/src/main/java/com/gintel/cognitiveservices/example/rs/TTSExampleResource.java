package com.gintel.cognitiveservices.example.rs;

import javax.ws.rs.core.Response;


import com.gintel.cognitiveservices.example.ExampleController;

public class TTSExampleResource implements TTSExample {

    private ExampleController controller;

    public TTSExampleResource(ExampleController controller) {
        this.controller = controller;
    }

    @Override
    public Response echo(String language, String voiceName, String text) throws Exception {

        return Response.ok().entity(controller.textToSpeech(language, voiceName, text)).build();
    }
}
