package com.gintel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.gintel.cognitiveservices.core.translation.Translation;
import com.gintel.cognitiveservices.core.translation.types.TranslationResult;
import com.gintel.cognitiveservices.core.translation.types.TranslationStatus;


public class AzureTranslationService implements Translation {
    private static final Logger logger = LoggerFactory.getLogger(AzureTranslationService.class);
    private AzureTranslationConfig serviceConfig;

    public AzureTranslationService(AzureTranslationConfig serviceConfig) {
        this.serviceConfig = serviceConfig;

        logger.info("region is {}", serviceConfig.region());
     }

    @Override
    public TranslationResult translation(String text, String fromLanguage, String toLanguage) {

        try {
        AzureKeyCredential credential = new AzureKeyCredential(serviceConfig.subscriptionKey());

        TextTranslationClient client = new TextTranslationClientBuilder().region(serviceConfig.region()).credential(credential).buildClient();
        
        List<String> targetLanguages = new ArrayList<>();
        targetLanguages.add(toLanguage);

        List<String> content = new ArrayList<>();
        content.add(text);

        TranslateOptions translateOptions = new TranslateOptions();

        translateOptions.addTargetLanguage(toLanguage);
        translateOptions.setSourceLanguage(fromLanguage);


        List<TranslatedTextItem> translations = client.translate(content, translateOptions);

        for (TranslatedTextItem translation : translations) {
            for (TranslationText textTranslation : translation.getTranslations()) {
                return new TranslationResult(TranslationStatus.OK, textTranslation.getText());
            }
        }
        } catch (Exception ex) {
            logger.error("Exception in translation", ex);
        }
        return new TranslationResult(TranslationStatus.ERROR, null);
    }
}
