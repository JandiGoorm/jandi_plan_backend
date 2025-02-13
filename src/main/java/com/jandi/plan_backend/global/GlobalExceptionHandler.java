package com.jandi.plan_backend.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 클래스.
 * 애플리케이션 전반에서 발생하는 RuntimeException(및 그 하위 예외)을 잡아서
 * 클라이언트에게 표준화된 에러 응답을 보내는 역할을 함.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 모든 RuntimeException을 처리하는 메서드.
     * 서비스나 컨트롤러에서 RuntimeException이 발생하면 이 메서드가 호출됨.
     *
     * @param ex 발생한 RuntimeException 객체
     * @return 에러 정보를 담은 ResponseEntity 객체 (HTTP 상태 코드, 에러 메시지, 타임스탬프 포함)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        // 에러 응답 정보를 담기 위한 Map 생성
        Map<String, Object> errorResponse = new HashMap<>();

        // HTTP 상태 코드를 Map에 저장 (여기서는 Bad Request: 400)
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());

        // HTTP 상태 메시지를 Map에 저장 ("Bad Request")
        errorResponse.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());

        // 예외 메시지를 Map에 저장 (발생한 예외의 상세 메시지)
        errorResponse.put("message", ex.getMessage());

        // 현재 시간을 에러 발생 시각으로 Map에 저장
        errorResponse.put("timestamp", LocalDateTime.now());

        // 위의 정보를 포함하는 ResponseEntity를 생성하고 400 BAD_REQUEST 상태로 반환
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
