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

    // 기본 max_tokens를 넉넉히 (예: 512)
    private static final int DEFAULT_MAX_TOKENS = 512;

    // temperature=0 (정확한 JSON 유도)
    private static final double TEMPERATURE = 0.0;

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    /**
     * 기존: askQuestion
     * -> 채팅 기반으로, modelName / max_tokens / temperature 등을 지정하여 요청
     */
    public OpenAiResponseDTO askQuestion(OpenAiRequestDTO requestDTO) {
        return askChatCompletion(requestDTO.getPrompt(), requestDTO.getMax_tokens());
    }

    /**
     * 추가: ChatCompletion 전용 메서드
     * - prompt, maxTokens, temperature=0으로 호출
     */
    public OpenAiResponseDTO askChatCompletion(String prompt, int maxTokens) {
        if (maxTokens <= 0) {
            maxTokens = DEFAULT_MAX_TOKENS;
        }
        log.info("OpenAI API 호출: prompt = {}", prompt);

        ChatPayloadDTO payload = new ChatPayloadDTO(
                modelName,
                Collections.singletonList(new ChatPayloadDTO.Message("user", prompt)),
                maxTokens,
                TEMPERATURE
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
