package com.gintel.cognitiveservices.core.openai.types;

import com.azure.ai.openai.models.ChatRequestMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatBotContext {
    private List<ChatRequestMessage> conversation;
    private List<Integer> messageTokens = new ArrayList<>();

    public void addMessages(List<ChatRequestMessage> chatMessages) {
        if (chatMessages != null) {this.conversation = chatMessages;}
    }

    public List<ChatRequestMessage> getMessages(){
        return this.conversation;
    }

    public void addTokenCost(Integer tokens) {
        this.messageTokens.add(tokens);
    }

    public void removeTokenCost(Integer index) {
        this.messageTokens.remove(index);
    }

    public List<Integer> getMessageTokens(){
        return this.messageTokens;
    }
}