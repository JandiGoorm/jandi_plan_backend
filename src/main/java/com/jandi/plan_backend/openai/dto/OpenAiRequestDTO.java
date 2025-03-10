package com.jandi.plan_backend.openai.dto;

import lombok.Data;

@Data
public class OpenAiRequestDTO {
    // prompt (질문) 내용
    private String prompt;
    // 최대 토큰 수 (출력 길이)
    private int max_tokens = 100;
    // 필요에 따라 temperature, top_p 등의 옵션을 추가할 수 있음
}
