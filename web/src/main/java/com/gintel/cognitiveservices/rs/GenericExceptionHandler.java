package com.gintel.cognitiveservices.rs;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.rs.types.ErrorDescriptionResponse;

public class GenericExceptionHandler implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionHandler.class);

    @Override
    public Response toResponse(final Throwable exception) {
        final String message = exception.getMessage() == null ? ""
                : "Message: " + exception.getMessage();
        logger.error("A throwable was caught:", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(getErrorDescriptionResponse(
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), message))
                .build();
    }

    private static ErrorDescriptionResponse getErrorDescriptionResponse(final int status,
            final String description) {
        ErrorDescriptionResponse errorDescriptionResponse = new ErrorDescriptionResponse(
                status * 1000, description);
        logger.info("Returning: {}", errorDescriptionResponse);
        return errorDescriptionResponse;
    }
}
