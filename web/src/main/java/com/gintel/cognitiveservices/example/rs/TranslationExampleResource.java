package com.gintel.cognitiveservices.example.rs;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.gintel.cognitiveservices.example.ExampleController;

public class TranslationExampleResource implements TranslationExample{
    private ExampleController controller;

    @Context
    private HttpServletRequest request;

    public TranslationExampleResource(ExampleController controller) {
        this.controller = controller;
    }

    @Override
    public Response echo(String text, String fromLanguage, String toLanguage) throws Exception {

        return Response.ok().entity(controller.translation(text, fromLanguage, toLanguage)).build();
    }
}
