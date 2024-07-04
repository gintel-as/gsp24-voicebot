package com.gintel.cognitiveservices.example.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("example/translation")
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface TranslationExample {
    @GET
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.WILDCARD })
    Response echo(
        @QueryParam("text") String text,
        @QueryParam("fromLanguage") String fromLanguage,
        @QueryParam("toLanguage") String toLanguage)
                throws Exception;
}