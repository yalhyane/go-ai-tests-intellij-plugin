package com.yalhyane.intellij.goaitests;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

public class PromptService {
    private static final String DEFAULT_OPENAI_MODEL = "gpt-3.5-turbo";
    private OpenAiService openAiService = null;
    private String openAiModel = DEFAULT_OPENAI_MODEL;

    public PromptService(String token) {
        this.openAiService = new OpenAiService(token);
    }


    public PromptService(String token, String model) {
        this.openAiService = new OpenAiService(token);
        this.openAiModel = model;
    }

    public String executeWithContext(String blockType, String code) throws Exception {

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(openAiModel)
                .temperature(1.0)
                .maxTokens(200)
                .messages(
                        List.of(
                                new ChatMessage("system", this.getSystemMessage(blockType)),
                                new ChatMessage("user", code)
                        )).build();

        List<ChatCompletionChoice> choices = openAiService.createChatCompletion(chatCompletionRequest)
                .getChoices();


        if (choices.isEmpty()) {
            throw new Exception("Empty response");
        }

        return choices.get(0).getMessage().getContent();

    }

    public String execute(String blockType, String code) throws Exception {

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(openAiModel)
                .temperature(0.8)
                .messages(
                        List.of(
                                new ChatMessage("user", getPrompt(code, blockType))
                        )).build();

        List<ChatCompletionChoice> choices = openAiService.createChatCompletion(chatCompletionRequest)
                .getChoices();


        if (choices.isEmpty()) {
            throw new Exception("Empty response");
        }

        return choices.get(0).getMessage().getContent();

    }



    private String getPrompt(String blockCode, String blockType) {
        return "I want you to act as a Senior Golang engineer"
                .concat("I will provide you " + blockType + " code")
                .concat("and I want you to respond with a comprehensive golang tests for a provided code.")
                .concat("The response should only contain valid golang code with no explanations")
                .concat("Here is the code to test:\n")
                .concat(blockCode);
    }



    private String getSystemMessage(String blockType) {
        return "I want you to act as a Senior Golang engineer"
                .concat("I will provide you " + blockType + " code")
                .concat("and I want you to respond with a comprehensive golang tests for a provided code.")
                .concat("The response should only contain valid golang code with no explanations.");
    }
}
