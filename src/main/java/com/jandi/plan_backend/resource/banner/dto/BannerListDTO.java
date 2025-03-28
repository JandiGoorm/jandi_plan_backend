package com.jandi.plan_backend.resource.banner.dto;

import com.jandi.plan_backend.resource.banner.entity.Banner;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BannerListDTO {
    private final Integer bannerId;
    private final LocalDateTime createdAt;
    private final String title;
    private final String subtitle;
    private final String linkUrl;
    private final String imageUrl;

    public BannerListDTO(Banner banner, String imageUrl) {
        this.bannerId = banner.getBannerId();
        this.createdAt = banner.getCreatedAt();
        this.title = banner.getTitle();
        this.subtitle = banner.getSubtitle();
        this.linkUrl = banner.getLinkUrl();
        this.imageUrl = imageUrl;
    }
}
