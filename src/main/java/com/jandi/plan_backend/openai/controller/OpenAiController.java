package com.jandi.plan_backend.openai.controller;

import com.jandi.plan_backend.openai.dto.OpenAiRequestDTO;
import com.jandi.plan_backend.openai.dto.OpenAiResponseDTO;
import com.jandi.plan_backend.openai.service.OpenAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/openai")
public class OpenAiController {

    private final OpenAiService openAiService;

    public OpenAiController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    /**
     * 질문에 대해 OpenAI API를 호출하여 답변을 반환합니다.
     * 모델은 "gpt-4o-mini"를 사용합니다.
     *
     * @param requestDTO 질문 및 옵션(prompt, max_tokens 등)을 담은 DTO
     * @return OpenAiResponseDTO 형식의 응답
     */
    @PostMapping("/ask")
    public ResponseEntity<OpenAiResponseDTO> askQuestion(@RequestBody OpenAiRequestDTO requestDTO) {
        OpenAiResponseDTO response = openAiService.askQuestion(requestDTO);
        return ResponseEntity.ok(response);
    }
}
