/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LLMConfig {

  @Value("${spring.ai.openai.api-key}")
  private String openaiApiKey;

  @Primary
  @Bean
  public OpenAiApi openAiApi() {
    return OpenAiApi.builder().apiKey(openaiApiKey).build();
  }

  @Bean
  public OpenAiChatOptions openAiChatOptions() {
    return OpenAiChatOptions.builder()
        .model("gpt-4o-mini")
        .temperature(0.0)
        .topP(1.0)
        .maxCompletionTokens(1024)
        .frequencyPenalty(0.0)
        .presencePenalty(0.0)
        .build();
  }

  @Bean
  public OpenAiChatModel chatModel(OpenAiApi openAiApi, OpenAiChatOptions options) {
    return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(options).build();
  }

  @Bean
  public ChatClient chatClient(OpenAiChatModel chatModel) {
    return ChatClient.create(chatModel);
  }

  @Bean
  public EmbeddingModel embeddingModel(OpenAiApi openAiApi) {
    return new OpenAiEmbeddingModel(
        openAiApi,
        MetadataMode.EMBED,
        OpenAiEmbeddingOptions.builder().model("text-embedding-3-small").build());
  }
}
