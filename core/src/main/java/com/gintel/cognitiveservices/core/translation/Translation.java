package com.gintel.cognitiveservices.core.translation;


import com.gintel.cognitiveservices.core.translation.types.TranslationResult;
import com.gintel.cognitiveservices.service.Service;

public interface Translation extends Service {
    TranslationResult translation(String text, String fromLanguage, String toLanguage);
    
}