package com.gintel.cognitiveservices.core.communication;

public interface EventHandler<T> {
    void onEvent(Object sender, T e);
}
