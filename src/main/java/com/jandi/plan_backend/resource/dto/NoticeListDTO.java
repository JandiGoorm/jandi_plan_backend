package com.jandi.plan_backend.resource.dto;

import com.jandi.plan_backend.resource.entity.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeListDTO {
    private final Integer noticeId;
    private final LocalDateTime createdAt;
    private final String title;
    private final String contents;

    public NoticeListDTO(Notice notice) {
        this.noticeId = notice.getNoticeId();
        this.createdAt = notice.getCreatedAt();
        this.title = notice.getTitle();
        this.contents = notice.getContents();
    }
}
