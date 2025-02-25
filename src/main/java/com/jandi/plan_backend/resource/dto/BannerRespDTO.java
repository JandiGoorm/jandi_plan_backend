package com.jandi.plan_backend.resource.dto;

import com.jandi.plan_backend.resource.entity.Banner;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BannerRespDTO {
    private final Integer bannerId;
    private final LocalDateTime createdAt;
    private final String title;
    private final String linkUrl;
    private final String imageUrl;

    public BannerRespDTO(Banner banner, String imageUrl) {
        this.bannerId = banner.getBannerId();
        this.createdAt = banner.getCreatedAt();
        this.title = banner.getTitle();
        this.linkUrl = banner.getLinkUrl();
        this.imageUrl = imageUrl;
    }
}
