package com.gintel;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gintel.cognitiveservices.core.translation.Translation;
import com.gintel.cognitiveservices.core.translation.types.TranslationResult;
import com.gintel.cognitiveservices.core.translation.types.TranslationStatus;

public class AzureTranslationService implements Translation {
     private static final Logger logger = LoggerFactory.getLogger(AzureTranslationService.class);
     private AzureTranslationConfig serviceConfig;
     private String key = serviceConfig.subscriptionKey();
     private String region = serviceConfig.region();
     private ObjectMapper mapper = new ObjectMapper();


     public AzureTranslationService(AzureTranslationConfig serviceConfig) {
        this.serviceConfig = serviceConfig;

        logger.info("region is {}", region);
     }

    @Override
    public TranslationResult translation(String text, Optional<String> fromLanguage, String toLanguage) {
        //TODO: implement translation
        throw new UnsupportedOperationException();
    }
}
