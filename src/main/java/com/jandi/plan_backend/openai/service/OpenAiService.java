package com.jandi.plan_backend.openai.service;

import com.jandi.plan_backend.openai.dto.ChatPayloadDTO;
import com.jandi.plan_backend.openai.dto.OpenAiRequestDTO;
import com.jandi.plan_backend.openai.dto.OpenAiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Service
@Slf4j
public class OpenAiService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    // 사용하려는 모델 이름 (가상의 "gpt-4o-mini")
    private final String modelName = "gpt-4o-mini";

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    /**
     * OpenAI API를 호출하여 질문에 대한 답변을 반환합니다.
     * 모델은 gpt-4o-mini를 사용하며, 채팅 엔드포인트를 사용합니다.
     *
     * @param requestDTO 질문 및 옵션을 담은 DTO (prompt, max_tokens 등)
     * @return OpenAiResponseDTO API 응답
     */
    public OpenAiResponseDTO askQuestion(OpenAiRequestDTO requestDTO) {
        log.info("OpenAI API 호출: prompt = {}", requestDTO.getPrompt());

        // 채팅 엔드포인트 요청 본문 생성: 메시지 배열로 구성 (role "user")
        ChatPayloadDTO payload = new ChatPayloadDTO(
                modelName,
                Collections.singletonList(new ChatPayloadDTO.Message("user", requestDTO.getPrompt())),
                requestDTO.getMax_tokens()
        );

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("OpenAI API error: {}", errorBody);
                            return clientResponse.createException();
                        })
                )
                .bodyToMono(OpenAiResponseDTO.class)
                .block();
    }
}
