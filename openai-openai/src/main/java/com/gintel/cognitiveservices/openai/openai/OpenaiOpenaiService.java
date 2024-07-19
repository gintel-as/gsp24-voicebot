package com.gintel.cognitiveservices.openai.openai;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.core.openai.Openai;
import com.gintel.cognitiveservices.core.openai.types.ChatBotContext;
import com.gintel.cognitiveservices.core.openai.types.InputFormat;
import com.gintel.cognitiveservices.core.openai.types.OutputFormat;
import com.gintel.cognitiveservices.core.openai.types.OpenaiResult;
import com.gintel.cognitiveservices.core.openai.types.OpenaiStatus;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.KeyCredential;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;

public class OpenaiOpenaiService implements Openai {
    private static final Logger logger = LoggerFactory.getLogger(OpenaiOpenaiService.class);

    private OpenaiOpenaiConfig serviceConfig;

    public String getProvider() {
        return "openai";
    }

    public OpenaiOpenaiService(OpenaiOpenaiConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    // Pretty much identical implementation as full-azure OpenAI, as this is using
    // an Azure framework for accessing OpenAI's own API
    @Override
    public OpenaiResult openai(String text, ChatBotContext ctx, InputFormat input, OutputFormat output) {
        try {
            String openaiOpenaiKey = serviceConfig.subscriptionKey();
            if (openaiOpenaiKey == null || openaiOpenaiKey.isEmpty()) {
                logger.error("OpenAI subscription key is null or empty.");
                return new OpenaiResult(OpenaiStatus.ERROR, "OpenAI subscription key is missing.", text);
            }
            String deploymentOrModelId = "gpt-3.5-turbo";

            EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
            Encoding enc = registry.getEncoding(EncodingType.CL100K_BASE);

            OpenAIClient client = new OpenAIClientBuilder()
                    .credential(new KeyCredential(openaiOpenaiKey))
                    .buildClient();

            List<ChatRequestMessage> chatMessages = new ArrayList<>();
            if (ctx.getMessages() != null) {
                chatMessages = ctx.getMessages();
            } else {
                chatMessages.add(new ChatRequestSystemMessage(
                        "You are a helpful assistant, speaking in a conversational language."));
                ctx.addTokenCost(
                        enc.encode("You are a helpful assistant, speaking in a conversational language.").size());
            }

            chatMessages.add(new ChatRequestUserMessage(text));
            if (chatMessages != null) {
                ctx.addMessages(chatMessages);
                ctx.addTokenCost(enc.encode(text).size());
            }

            ChatCompletionsOptions completionsOptions = new ChatCompletionsOptions(chatMessages);
            completionsOptions.setMaxTokens(1000);

            ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, completionsOptions);
            CompletionsUsage usage = chatCompletions.getUsage();
            if (usage != null) {
                logger.info("Prompt tokens used: {}", usage.getPromptTokens());
                logger.info("Completion tokens used: {}", usage.getCompletionTokens());
                logger.info("Total tokens used: {}", usage.getTotalTokens());

                if (usage.getTotalTokens() >= completionsOptions.getMaxTokens()) {
                    Integer messages = 1;
                    Integer diff = usage.getTotalTokens() - completionsOptions.getMaxTokens();
                    while (messages < ctx.getMessageTokens().size() - 1) {
                        Integer sum = 0;
                        for (int i = 0; i < messages; i++) {
                            sum += ctx.getMessageTokens().get(i + 1);
                        }
                        if (sum > diff) {
                            List<Integer> tokes = ctx.getMessageTokens();
                            for (int j = 0; j <= messages; j++) {
                                tokes.remove(messages - j);
                                chatMessages.remove(messages - j);
                            }
                            ctx.setTokenCost(tokes);
                            ctx.addMessages(chatMessages);
                            completionsOptions = new ChatCompletionsOptions(chatMessages);
                            chatCompletions = client.getChatCompletions(deploymentOrModelId, completionsOptions);
                            usage = chatCompletions.getUsage();
                            if (usage != null) {
                                logger.info("Deleted " + Math.addExact(messages, 1) + " messages");
                                logger.info("Prompt tokens used (after deletion): {}", usage.getPromptTokens());
                                logger.info("Completion tokens used (after deletion): {}", usage.getCompletionTokens());
                                logger.info("Total tokens used (after deletion): {}", usage.getTotalTokens());
                            }
                            String mld = "";
                            for (ChatChoice choice : chatCompletions.getChoices()) {
                                ChatResponseMessage message = choice.getMessage();
                                System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
                                System.out.println("Message:");
                                System.out.println(message.getContent());
                                if ("assistant".equals(message.getRole().toString())) {
                                    mld = message.getContent();
                                    chatMessages.add(new ChatRequestAssistantMessage(message.getContent()));
                                    ctx.addMessages(chatMessages);
                                    ctx.addTokenCost(enc.encode(message.getContent()).size());
                                }
                            }

                            return new OpenaiResult(OpenaiStatus.OK,
                                    "NB! Token management deleted older messages\n\n" + mld, text);
                        }
                        messages += 2;
                    }
                    ;
                    logger.warn("Maximum token limit reached with previous prompt.");
                    return new OpenaiResult(OpenaiStatus.ERROR, "Maximum token limit reached with previous prompt",
                            text);
                }
            }

            String mld = "";
            for (ChatChoice choice : chatCompletions.getChoices()) {
                ChatResponseMessage message = choice.getMessage();
                System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
                System.out.println("Message:");
                System.out.println(message.getContent());
                if ("assistant".equals(message.getRole().toString())) {
                    mld = message.getContent();
                    chatMessages.add(new ChatRequestAssistantMessage(message.getContent()));
                    ctx.addMessages(chatMessages);
                    ctx.addTokenCost(enc.encode(message.getContent()).size());
                }
            }

            return new OpenaiResult(OpenaiStatus.OK, mld, text);
        } catch (Exception ex) {
            logger.error("Failed to process openAI request: {}", text, ex);
            return new OpenaiResult(OpenaiStatus.ERROR, "Error processing request", text);
        }
    }
}