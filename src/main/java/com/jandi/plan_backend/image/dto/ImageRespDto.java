package com.jandi.plan_backend.image.dto;

import lombok.Data;

@Data
public class ImageRespDto {
    private Integer imageId;
    private String imageUrl;
    private String message;
}
