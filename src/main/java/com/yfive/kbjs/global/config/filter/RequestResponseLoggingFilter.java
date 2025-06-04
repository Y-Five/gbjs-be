/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.kbjs.global.config.filter;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // 요청 ID 생성 및 MDC에 추가
    String traceId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put("traceId", traceId);

    // 요청/응답 래핑
    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

    long startTime = System.currentTimeMillis();

    try {
      // 요청 로깅
      logRequest(requestWrapper);

      // 필터 체인 실행
      filterChain.doFilter(requestWrapper, responseWrapper);

      // 응답 로깅
      long duration = System.currentTimeMillis() - startTime;
      logResponse(responseWrapper, duration);

      // 응답 복원 (중요: 응답 내용을 읽은 후 복원해야 함)
      responseWrapper.copyBodyToResponse();
    } finally {
      MDC.remove("traceId");
    }
  }

  private void logRequest(ContentCachingRequestWrapper request) {
    String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
    log.info(
        "REQUEST: {} {} {}",
        request.getMethod(),
        request.getRequestURI() + queryString,
        request.getRemoteAddr());
    log.debug("REQUEST HEADERS: {}", getHeaders(request));
  }

  private void logResponse(ContentCachingResponseWrapper response, long duration) {
    log.info("RESPONSE: {} ({}ms)", response.getStatus(), duration);
  }

  private String getHeaders(HttpServletRequest request) {
    StringBuilder headers = new StringBuilder();
    request
        .getHeaderNames()
        .asIterator()
        .forEachRemaining(
            headerName -> {
              headers
                  .append(headerName)
                  .append(": ")
                  .append(request.getHeader(headerName))
                  .append(", ");
            });
    return headers.toString();
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // 정적 리소스나 액추에이터 엔드포인트는 로깅에서 제외
    return path.contains("/actuator")
        || path.contains("/swagger-ui")
        || path.contains("/api-docs")
        || path.contains("/v3/api-docs")
        || path.contains("/webjars")
        || path.contains("/favicon.ico");
  }
}
