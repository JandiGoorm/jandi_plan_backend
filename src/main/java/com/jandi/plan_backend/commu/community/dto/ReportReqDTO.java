package com.jandi.plan_backend.commu.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReportReqDTO {
    @NotBlank(message = "신고 내용은 필수입니다")
    @Size(min = 1, max = 1000, message = "신고 내용은 1~1000자여야 합니다")
    private String contents;
}
