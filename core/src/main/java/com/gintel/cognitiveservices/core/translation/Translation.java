package com.gintel.cognitiveservices.core.translation;

import com.gintel.cognitiveservices.core.translation.types.InputFormat;
import com.gintel.cognitiveservices.core.translation.types.OutputFormat;
import com.gintel.cognitiveservices.core.translation.types.TranslationResult;
import com.gintel.cognitiveservices.service.Service;

public interface Translation extends Service {
    TranslationResult translation(String language, InputFormat input, OutputFormat output);
    
}