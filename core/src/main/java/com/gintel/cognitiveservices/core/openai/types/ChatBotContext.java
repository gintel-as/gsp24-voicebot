package com.gintel.cognitiveservices.core.openai.types;

import com.azure.ai.openai.models.ChatRequestMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatBotContext {
    private List<ChatRequestMessage> conversation;
    private List<Integer> messageTokens = new ArrayList<>();
    private String language = "none";
    private String chosenTts = "azure";
    private String chosenAi = "azure";

    public void addMessages(List<ChatRequestMessage> chatMessages) {
        if (chatMessages != null) {
            this.conversation = chatMessages;
        }
    }

    public List<ChatRequestMessage> getMessages() {
        return this.conversation;
    }

    public void addTokenCost(Integer tokens) {
        this.messageTokens.add(tokens);
    }

    public void setTokenCost(List<Integer> tokens) {
        this.messageTokens = tokens;
    }

    public List<Integer> getMessageTokens() {
        return this.messageTokens;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setChosenTts(String chosenTts) {
        this.chosenTts = chosenTts;
    }

    public String getChosenTts() {
        return this.chosenTts;
    }

    public void setChosenAi(String chosenAi) {
        this.chosenAi = chosenAi;
    }

    public String getChosenAi() {
        return this.chosenAi;
    }
}