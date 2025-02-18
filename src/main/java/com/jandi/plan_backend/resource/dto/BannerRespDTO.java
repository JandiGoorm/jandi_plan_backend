package com.jandi.plan_backend.resource.dto;

import com.jandi.plan_backend.resource.entity.Banner;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BannerRespDTO {
    private final Integer bannerId;
    private final LocalDateTime createdAt;
    private final String title;
    private final String imageUrl;
    private final String linkUrl;

    public BannerRespDTO(Banner banner) {
        this.bannerId = banner.getBannerId();
        this.createdAt = banner.getCreatedAt();
        this.title = banner.getTitle();
        this.imageUrl = banner.getImageUrl();
        this.linkUrl = banner.getLinkUrl();
    }
}
