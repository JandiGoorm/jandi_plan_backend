package com.jandi.plan_backend.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatPayloadDTO {
    private String model;
    private List<Message> messages;
    private int max_tokens;
    private double temperature;

    @Data
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
