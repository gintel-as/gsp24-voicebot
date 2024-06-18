package com.gintel.cognitiveservices.example.rs;

import javax.ws.rs.core.Response;

import com.gintel.cognitiveservices.example.ExampleController;

public class OpenaiExampleResource implements OpenaiExample {

    private ExampleController controller;

    public OpenaiExampleResource(ExampleController controller) {
        this.controller = controller;
    }

    @Override
    public Response echo(String text) throws Exception {

        return Response.ok().entity(controller.openai(text)).build();
    }
}
