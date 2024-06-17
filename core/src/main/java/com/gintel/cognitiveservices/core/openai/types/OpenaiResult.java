package com.gintel.cognitiveservices.core.openai.types;

public class OpenaiResult {
    private OpenaiStatus status;
    private String response;
    private String input;

    public OpenaiResult() {
    }

    public OpenaiResult(OpenaiStatus status, String response, String input) {
        this.status = status;
        this.response = response;
        this.input = input;
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

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}