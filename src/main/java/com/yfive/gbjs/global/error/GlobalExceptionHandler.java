/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.error;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.yfive.gbjs.global.common.response.ApiResponse;
import com.yfive.gbjs.global.common.response.ResponseCode;
import com.yfive.gbjs.global.error.exception.InvalidTokenException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 400 Bad Request
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.error("MethodArgumentNotValidException", e);
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ResponseCode.VALIDATION_ERROR, "입력값 검증에 실패했습니다."));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e) {
    log.error("MissingServletRequestParameterException", e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.error(
                ResponseCode.BAD_REQUEST, "필수 파라미터가 누락되었습니다: " + e.getParameterName()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    log.error("MethodArgumentTypeMismatchException", e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ResponseCode.BAD_REQUEST, "파라미터 타입이 일치하지 않습니다: " + e.getName()));
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
    log.error("BindException", e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ResponseCode.BAD_REQUEST, "잘못된 요청입니다."));
  }

  // 401 Unauthorized
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
      AuthenticationException e) {
    log.error("AuthenticationException", e);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ResponseCode.UNAUTHORIZED));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
      BadCredentialsException e) {
    log.error("BadCredentialsException", e);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ResponseCode.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."));
  }

  // JWT 관련 예외
  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(InvalidTokenException e) {
    log.error("InvalidTokenException: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ResponseCode.INVALID_TOKEN, e.getMessage()));
  }

  @ExceptionHandler(SignatureException.class)
  public ResponseEntity<ApiResponse<Void>> handleSignatureException(SignatureException e) {
    log.error("SignatureException", e);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ResponseCode.INVALID_TOKEN));
  }

  @ExceptionHandler(MalformedJwtException.class)
  public ResponseEntity<ApiResponse<Void>> handleMalformedJwtException(MalformedJwtException e) {
    log.error("MalformedJwtException", e);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ResponseCode.INVALID_TOKEN));
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException e) {
    log.error("ExpiredJwtException", e);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ResponseCode.EXPIRED_TOKEN));
  }

  @ExceptionHandler(UnsupportedJwtException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnsupportedJwtException(
      UnsupportedJwtException e) {
    log.error("UnsupportedJwtException", e);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ResponseCode.INVALID_TOKEN));
  }

  // 403 Forbidden
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
    log.error("AccessDeniedException", e);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(ResponseCode.FORBIDDEN));
  }

  // 404 Not Found
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
      NoHandlerFoundException e) {
    log.error("NoHandlerFoundException", e);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ResponseCode.NOT_FOUND));
  }

  // 405 Method Not Allowed
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    log.error("HttpRequestMethodNotSupportedException", e);
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ApiResponse.error(ResponseCode.METHOD_NOT_ALLOWED));
  }

  // 500 Internal Server Error
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(
      Exception e, HttpServletRequest request) {
    log.error("Exception: {} {}", request.getMethod(), request.getRequestURI(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR));
  }
}
