package com.jandi.plan_backend.util.service;

/**
 * 구글 지도 api 오류 발생 시 메시지 포함하여 오류 출력
 */
public class GoogleApiException extends RuntimeException {

  // 기존: cause만 받는 생성자
  public GoogleApiException(Throwable cause) {
    super("Error occurred while calling Google Places API: " + cause.getMessage(), cause);
  }

  // 추가: 메시지 + cause를 함께 받는 생성자
  public GoogleApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
