package com.jandi.plan_backend.resource.banner.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BannerReqDTO {
    private MultipartFile file;
    private String title;
    private String subTitle;
    private String linkUrl;

    public BannerReqDTO(MultipartFile file, String title, String subTitle, String linkUrl) {
        this.file = file;
        this.title = title;
        this.subTitle = subTitle;
        this.linkUrl = linkUrl;
    }
}
