package com.gintel.cognitiveservices.core.stt;

public interface EventHandler<T> {
    void onEvent(Object sender, T e);
}
