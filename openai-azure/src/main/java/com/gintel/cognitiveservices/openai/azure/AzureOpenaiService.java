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
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
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
            if (ctx.getMessages() != null){
                chatMessages = ctx.getMessages();
            } else {
                chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. Reply in a conversational language with only the necessary content."));
                ctx.addTokenCost("You are a helpful assistant.".length()*4);
            }

            chatMessages.add(new ChatRequestUserMessage(text));
            if (chatMessages != null) {
                ctx.addMessages(chatMessages);
                ctx.addTokenCost(text.length()*4);
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
                    while(messages < ctx.getMessageTokens().size()-1){
                        Integer sum = 0;
                        for (int i = 0; i < messages; i++) {
                            sum += ctx.getMessageTokens().get(i+1);
                        }
                        if (sum > diff) {
                            for (int j = messages; j > 0; j--){
                                ctx.removeTokenCost(j);
                                chatMessages.remove(j);
                                ctx.addMessages(chatMessages);
                            }
                            completionsOptions = new ChatCompletionsOptions(chatMessages);
                            chatCompletions = client.getChatCompletions(deploymentOrModelId, completionsOptions);
                            usage = chatCompletions.getUsage();
                            if (usage != null) {
                                logger.info("Deleted " + messages + " messages");
                                logger.info("Prompt tokens used (after deletion): {}", usage.getPromptTokens());
                                logger.info("Completion tokens used (after deletion): {}", usage.getCompletionTokens());
                                logger.info("Total tokens used (after deletion): {}", usage.getTotalTokens());
                                logger.info("" + chatMessages);
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
                                    ctx.addTokenCost(message.getContent().length()*4);
                                }
                            }

                            return new OpenaiResult(OpenaiStatus.OK, "NB! Token management deleted older messages\n\n" + mld, text);
                        }
                        messages += 2;
                    };
                    logger.warn("Maximum token limit reached with previous prompt.");
                    return new OpenaiResult(OpenaiStatus.ERROR, "Maximum token limit reached with previous prompt", text);
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
                    ctx.addTokenCost(message.getContent().length()*4);
                }
            }

            return new OpenaiResult(OpenaiStatus.OK, mld, text);
        } catch (Exception ex) {
            logger.error("Failed to process openAI request: {}", text, ex);
            return new OpenaiResult(OpenaiStatus.ERROR, "Error processing request", text);
        }
    }
}