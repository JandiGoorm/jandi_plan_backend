package com.jandi.plan_backend.util.service;

public class GoogleApiException extends RuntimeException {
  // 구글 지도 api 오류 발생 시 메시지 포함하여 오류 출력
  public GoogleApiException(Throwable cause) {
    super("Error occurred while calling Google Places API: " + cause.getMessage(), cause);
  }
}
