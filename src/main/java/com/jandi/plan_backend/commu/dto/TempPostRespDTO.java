package com.jandi.plan_backend.commu.dto;

import lombok.Data;

@Data
public class TempPostRespDTO {
    private int tempPostId;
    private String message;

    public TempPostRespDTO(int tempPostId, String message) {
        this.tempPostId = tempPostId;
        this.message = message;
    }
}
