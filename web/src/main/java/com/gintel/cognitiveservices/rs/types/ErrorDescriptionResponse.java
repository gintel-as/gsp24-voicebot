package com.gintel.cognitiveservices.rs.types;

import java.util.StringJoiner;

public class ErrorDescriptionResponse {
    private int errorCode;
    private String description;

    public ErrorDescriptionResponse() {
    }

    public ErrorDescriptionResponse(final int errorCode, final String description) {
        this.errorCode = errorCode;
        this.description = description;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ErrorDescriptionResponse.class.getSimpleName() + "[", "]")
                .add("errorCode=" + errorCode).add("description='" + description + "'").toString();
    }
}