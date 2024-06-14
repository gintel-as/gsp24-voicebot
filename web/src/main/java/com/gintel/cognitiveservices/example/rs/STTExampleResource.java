package com.gintel.cognitiveservices.example.rs;

import javax.ws.rs.core.Response;

import com.gintel.cognitiveservices.example.ExampleController;

public class STTExampleResource implements STTExample {

    private ExampleController controller;

    public STTExampleResource(ExampleController controller) {
        this.controller = controller;
    }

    @Override
    public Response echo(String language) throws Exception {

        return Response.ok().entity(controller.speechToText()).build();
    }
}
