package com.jandi.plan_backend.commu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReportReqDTO {
    @NotNull
    private String contents;
}
