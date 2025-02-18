package com.jandi.plan_backend.resource.dto;

import lombok.Getter;

/**
 * 배너 작성 시 클라이언트로부터 전달되는 데이터를 담는 DTO
 */
@Getter
public class BannerWritePostDTO {
    private String title;
    private String imageUrl;
    private String linkUrl;
}
