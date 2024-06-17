package com.gintel.cognitiveservices.openai.azure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.openai.Openai;
import com.gintel.cognitiveservices.core.openai.types.InputFormat;
import com.gintel.cognitiveservices.core.openai.types.OutputFormat;
import com.gintel.cognitiveservices.core.openai.types.OpenaiResult;
import com.gintel.cognitiveservices.core.openai.types.OpenaiStatus;

public class AzureOpenaiService implements Openai{
    private static final Logger logger = LoggerFactory.getLogger(AzureOpenaiService.class);

    private AzureOpenaiConfig serviceConfig;

    public AzureOpenaiService(AzureOpenaiConfig serviceConfig) {
        this.serviceConfig = serviceConfig;

        logger.info("region is {}", serviceConfig.region());
    }

    @Override
    public OpenaiResult openai(String text, InputFormat input, OutputFormat output) {
        
        String serviceRegion = serviceConfig.region();

        String lang = "Don't reply to this";

        System.out.println("OpenAi fungerer");

        if (text != null) {
            lang = text.replace(new StringBuilder().append('"'), "");
        }
        return null;
    }
}