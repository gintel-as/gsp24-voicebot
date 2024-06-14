package com.gintel.cognitiveservices.rs.filters;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class LogRequestFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LogRequestFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        if (logger.isTraceEnabled()) {

            final MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo()
                    .getPathParameters();
            final MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo()
                    .getQueryParameters();

            logger.trace(new LogContext()
                    .add("Method name", resourceInfo.getResourceMethod().getName())
                    .add("URL", requestContext.getUriInfo().getAbsolutePath())
                    .addNonNull("JSON", getJson(requestContext))
                    .addNonNull("Path Parameters",
                            !pathParameters.isEmpty() ? pathParameters : null)
                    .addNonNull("Query Parameters",
                            !queryParameters.isEmpty() ? queryParameters.entrySet() : null)
                    .add("Headers",
                            requestContext.getHeaders().entrySet().stream()
                            .map(e -> e.getKey() + "=" + String.join(",", e.getValue()))
                            .collect(Collectors.joining("\n\t\t")))
                    .toString());
        }
    }

    private String getJson(ContainerRequestContext request) {
        if (request.getMediaType() == null
                || !request.getMediaType().toString().contains("application/json")) {
            return null;
        }

        try (Scanner s = new Scanner(request.getEntityStream(), "UTF-8").useDelimiter("\\A")) {
            final String jsonStr = s.hasNext() ? s.next() : null;
            if (jsonStr != null) {
                request.setEntityStream(
                        new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8)));
            }
            return jsonStr;
        }
    }

}