/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.domain.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.yfive.gbjs.domain.chat.dto.request.ChatRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

  private final VectorStore vectorStore;
  private final ChatClient chatClient;
  private static final double SIMILARITY_THRESHOLD = 0.3;
  private static final int TOP_K = 5;

  @Override
  public String ask(ChatRequest request) {
    try {
      log.info("사용자 질문: {}", request.getQuestion());
      log.info("VectorStore 클래스: {}", vectorStore.getClass().getName());

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
        return "관련된 데이터를 찾을 수 없습니다.";
      }

      // 벡터 검색 결과 로그(개발용)
      if (log.isDebugEnabled()) {
        docs.forEach(
            doc -> log.debug("검색 결과 docId={}, content={}", doc.getId(), doc.getFormattedContent()));
      }

      String context =
          docs.stream().map(Document::getFormattedContent).collect(Collectors.joining("\n---\n"));

      String prompt =
          """
              당신은 gbjs 서비스 전용 답변 시스템입니다.
              반드시 아래 제공된 참고 데이터만을 근거로 답변하세요.
              참고 데이터에 없는 내용은 '관련된 데이터를 찾을 수 없습니다.'라고 답하세요.

              사용자 질문: %s

              참고 데이터:
              %s
              """
              .formatted(request.getQuestion(), context);

      return chatClient
          .prompt()
          .system("너는 gbjs 데이터만 기반으로 답해야 한다. 데이터 밖 지식은 절대 사용하지 마라.")
          .user(prompt)
          .call()
          .content();

    } catch (Exception e) {
      log.error("챗봇 응답 생성 중 오류 발생", e);
      return "현재 답변을 생성할 수 없습니다. 나중에 다시 시도해주세요.";
    }
  }
}
