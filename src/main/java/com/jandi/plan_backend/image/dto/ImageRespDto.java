package com.jandi.plan_backend.image.dto;

import lombok.Data;

/**
 * 이미지 업로드 응답 정보를 담는 DTO.
 */
@Data
public class ImageRespDto {
    private Integer imageId;
    private String imageUrl;
    private String message;
}
