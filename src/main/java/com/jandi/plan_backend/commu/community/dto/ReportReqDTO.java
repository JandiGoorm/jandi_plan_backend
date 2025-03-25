package com.jandi.plan_backend.commu.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReportReqDTO {
    @NotNull
    private String contents;
}
