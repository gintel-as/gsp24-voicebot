package com.gintel.cognitiveservices.core.openai.types;

public class OpenaiResult {
    private OpenaiStatus status;
    private String response;

    public OpenaiResult() {
    }

    public OpenaiResult(OpenaiStatus status, String response) {
        this.status = status;
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public OpenaiStatus getStatus() {
        return status;
    }

    public void setStatus(OpenaiStatus status) {
        this.status = status;
    }
}