package com.gintel.cognitiveservices.service;

public interface Service {
    default String getServiceName() {
        return this.getClass().getSimpleName();
    }
}