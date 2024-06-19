package com.gintel.cognitiveservices.core.openai;

import com.gintel.cognitiveservices.core.openai.types.OpenaiResult;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.core.openai.types.InputFormat;
import com.gintel.cognitiveservices.core.openai.types.OutputFormat;

public interface Openai {
    OpenaiResult openai(String text, ChatBotContext ctx, InputFormat input, OutputFormat output);
}
