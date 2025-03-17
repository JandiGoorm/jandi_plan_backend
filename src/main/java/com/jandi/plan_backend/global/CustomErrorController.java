package com.jandi.plan_backend.global;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(WebRequest webRequest) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        int status = (int) attributes.getOrDefault("status", 500);

        String message;
        if (status == 401) {
            message = "토큰값 확인이 필요합니다.";
        } else if (status == 404) {
            message = "요청하신 경로를 찾을 수 없습니다.";
        } else {
            message = (String) attributes.getOrDefault("message",
                    attributes.getOrDefault("error", "알 수 없는 오류 발생"));
        }

        // 원하는 추가 정보를 설정할 수 있습니다.
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status);
        errorResponse.put("error", errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults()).get("error"));
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(status));
    }
}
