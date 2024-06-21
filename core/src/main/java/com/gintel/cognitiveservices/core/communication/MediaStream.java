package com.gintel.cognitiveservices.core.communication;

public interface MediaStream {
    void write(byte[] data);

    void close();
}
