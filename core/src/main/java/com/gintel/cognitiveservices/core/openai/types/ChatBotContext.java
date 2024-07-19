package com.gintel.cognitiveservices.core.openai.types;

import com.azure.ai.openai.models.ChatRequestMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatBotContext {
    private List<ChatRequestMessage> conversation; // Chat-history input used in both Azure's and OpenAI's AI-engine
    private List<Integer> messageTokens = new ArrayList<>(); // Tokens per-message, used for chat deletion in token
                                                             // management
    private String language = "none"; // Language used for translation and Google's and AWS's tts output
    private String chosenTts = "azure"; // TTS chosen in client dropdown
    private String chosenAi = "azure"; // AI-engine chosen in client dropdown

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