package com.jandi.plan_backend.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리를 담당하는 클래스.
 * RuntimeException을 비롯하여 다양한 예외를 한 곳에서 처리할 수 있음.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 모든 RuntimeException에 대해 처리.
     * 서비스/컨트롤러에서 throw new RuntimeException("에러메시지") 시 이쪽으로 옴.
     * HTTP 상태 코드, 상태 메시지, 예외 메시지, 타임스탬프를 포함하여 반환함.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        // HTTP 상태 코드
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        // HTTP 상태 메시지 (예: "Bad Request")
        errorResponse.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        // 예외 메시지
        errorResponse.put("message", ex.getMessage());
        // 예외 발생 시각
        errorResponse.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
