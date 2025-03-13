package com.jandi.plan_backend.googlePlace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecommPlaceReqDTO {

    // cityId가 null이거나 1보다 작은 경우 유효성 검증에서 오류 발생
    @NotNull(message = "cityId cannot be null")
    @Min(value = 1, message = "cityId must be >= 1")
    private Integer cityId;
}
