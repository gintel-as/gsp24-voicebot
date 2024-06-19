package com.gintel.cognitiveservices.core.stt.types;

public interface MediaStream {
    void write(byte[] data);

    void close();
}
