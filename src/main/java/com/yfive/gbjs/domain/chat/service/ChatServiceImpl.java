/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.chat.service;

import com.yfive.gbjs.domain.chat.dto.request.ChatRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

  private final VectorStore vectorStore;
  private final ChatClient chatClient;
  private static final double SIMILARITY_THRESHOLD = 0.2;
  private static final int TOP_K = 50;

  @Override
  public String ask(ChatRequest request) {
    try {
      log.info("사용자 질문: {}", request.getQuestion());

      // 유사도 검색
      SearchRequest searchRequest =
          SearchRequest.builder()
              .query(request.getQuestion())
              .similarityThreshold(SIMILARITY_THRESHOLD)
              .topK(TOP_K)
              .build();

      List<Document> docs = vectorStore.similaritySearch(searchRequest);
      log.info("검색된 문서 수: {}", docs.size());

      if (docs.isEmpty()) {
        log.info("질문 '{}'에 대해 검색된 문서가 없습니다.", request.getQuestion());
        String fallbackPrompt =
            """
                <task>
                  <role>You are an exclusive answering system for the 경북지색 service. The chatbot's character is a magpie (까치) named Chichi. Respond in character accordingly.</role>
                  <instruction>
                    - If the user's question is casual or conversational (e.g., greetings, "Hello?", "How are you?", "몇 살이야?"):
                        - Respond naturally in a friendly and sometimes humorous way, as Chichi the magpie would.
                    - If the user's question is about 경북지 reference data but no data is found, respond exactly with: "No relevant data found."
                    - If the answer exceeds the model's response length limit, provide a concise summary and end with "등등..." to indicate more information exists.
                  </instruction>
                  <user_question>
                    %s
                  </user_question>
                </task>
                """
                .formatted(request.getQuestion());
        return chatClient
            .prompt()
            .system("너는 경북지색 데이터만 기반으로 답해야 한다. 데이터 밖 지식은 절대 사용하지 마라. 단, 까치 이름은 치치이며, 캐릭터에 맞게 응답하라.")
            .user(fallbackPrompt)
            .call()
            .content();
      }

      String context =
          docs.stream().map(Document::getFormattedContent).collect(Collectors.joining("\n---\n"));

      String prompt =
          """
              <task>
                <role>You are an exclusive answering system for the 경북지색 service. The chatbot's character is a magpie (까치) named Chichi. Respond in character accordingly.</role>
                <instruction>
                  - If the user's question is about 경북지색 reference data, answer strictly using that data.
                  - If the user's question is casual or conversational (e.g., greetings, "Hello?", "How are you?"):
                      - Respond naturally in a friendly way as Chichi the magpie.
                      - Do not use any outside knowledge beyond simple conversational context.
                  - If 경북지색 data is required but not found, respond exactly with: "No relevant data found."
                  - If the answer exceeds the model's response length limit, provide a concise summary and end with "등등..." to indicate more information exists.
                </instruction>
                <user_question>
                  %s
                </user_question>
                <reference_data>
                  %s
                </reference_data>
              </task>
              """
              .formatted(request.getQuestion(), context);

      return chatClient
          .prompt()
          .system("너는 경북지색 데이터만 기반으로 답해야 한다. 데이터 밖 지식은 절대 사용하지 마라. 까치 이름은 치치이며, 캐릭터에 맞게 응답하라.")
          .user(prompt)
          .call()
          .content();

    } catch (Exception e) {
      log.error("챗봇 응답 생성 중 오류 발생", e);
      return "현재 답변을 생성할 수 없습니다. 나중에 다시 시도해주세요.";
    }
  }
}
