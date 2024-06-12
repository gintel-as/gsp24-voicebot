package com.gintel.cognitiveservices.config;

import org.aeonbits.owner.Preprocessor;

public class ConfigTrim implements Preprocessor {
    @Override
    public String process(String input) {
        return input != null ? input.trim() : null;
    }
}