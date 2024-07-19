package com.gintel.cognitiveservices.example.rs;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.example.ExampleController;

public class OpenaiExampleResource implements OpenaiExample {

    private ExampleController controller;

    @Context
    private HttpServletRequest request;

    public OpenaiExampleResource(ExampleController controller) {
        this.controller = controller;
    }

    @Override
    public Response echo(String text) throws Exception {
        ChatBotContext ctx;
        HttpSession session = request.getSession();

        // Initiates or updates ChatBotContext in relation to HTTP session
        if (session.getAttribute("ctx") != null) {
            ctx = (ChatBotContext) session.getAttribute("ctx");
        } else {
            ctx = new ChatBotContext();
            session.setAttribute("ctx", ctx);
        }
        return Response.ok().entity(controller.openai(text, ctx)).build();
    }
}