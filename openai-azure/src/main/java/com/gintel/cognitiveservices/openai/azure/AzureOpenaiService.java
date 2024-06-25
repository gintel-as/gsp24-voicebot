package com.gintel.cognitiveservices.openai.azure;

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
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AzureKeyCredential;

public class AzureOpenaiService implements Openai{
    private static final Logger logger = LoggerFactory.getLogger(AzureOpenaiService.class);

    private AzureOpenaiConfig serviceConfig;

    public AzureOpenaiService(AzureOpenaiConfig serviceConfig) {
        this.serviceConfig = serviceConfig;

        logger.info("region is {}", serviceConfig.region());
    }

    @Override
    public OpenaiResult openai(String text, ChatBotContext ctx, InputFormat input, OutputFormat output) {
        try {
            String azureOpenaiKey = serviceConfig.subscriptionKey();
            String endpoint = "https://gintel-openai-resource.openai.azure.com/";
            String deploymentOrModelId = "testDeployment";

            OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildClient();
            


            List<ChatRequestMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant."));
            if (ctx.getMessages() != null){chatMessages = ctx.getMessages();}

            chatMessages.add(new ChatRequestUserMessage(text));
            if (chatMessages != null) {ctx.addMessages(chatMessages);}


            ChatCompletionsOptions completionsOptions = new ChatCompletionsOptions(chatMessages);
            completionsOptions.setMaxTokens(4000);
            
            ChatCompletions chatCompletions = client.getChatCompletions(deploymentOrModelId, completionsOptions);
            CompletionsUsage usage = chatCompletions.getUsage();
            if (usage != null) {
                logger.info("Prompt tokens used: {}", usage.getPromptTokens());
                logger.info("Completion tokens used: {}", usage.getCompletionTokens());
                logger.info("Total tokens used: {}", usage.getTotalTokens());

                if (usage.getTotalTokens() >= completionsOptions.getMaxTokens()) {
                    logger.warn("The maximum number of tokens has been reached.");
                    return new OpenaiResult(OpenaiStatus.ERROR, "Maximum token limit reached", text);
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
                }
            }

            return new OpenaiResult(OpenaiStatus.OK, mld, text);
        } catch (Exception ex) {
            logger.error("Failed to process openAI request: {}", text, ex);
            return new OpenaiResult(OpenaiStatus.ERROR, "Error processing request", text);
        }
    }
}