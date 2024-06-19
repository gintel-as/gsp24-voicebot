package com.gintel.cognitiveservices.core.openai.types;

import com.azure.ai.openai.models.ChatRequestMessage;
import java.util.List;

public class ChatBotContext {
    private List<ChatRequestMessage> conversation;

    public void addMessages(List<ChatRequestMessage> chatMessages) {
        if (chatMessages != null) {this.conversation = chatMessages;}
    }

    public List<ChatRequestMessage> getMessages(){
        return this.conversation;
    }
}