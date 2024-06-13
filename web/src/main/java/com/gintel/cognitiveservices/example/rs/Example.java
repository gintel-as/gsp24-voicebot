package com.gintel.cognitiveservices.example.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("example/v1")
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface Example {
    @GET
    @Path("/")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.WILDCARD })
    Response echo(
            @QueryParam("language") String language,
            @QueryParam("voiceName") String voiceName,
            @QueryParam("text") String text)
                    throws Exception;
}

