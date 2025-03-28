package com.jandi.plan_backend.resource.notice.dto;

import com.jandi.plan_backend.resource.notice.entity.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeRespDTO {
    private final Integer noticeId;
    private final LocalDateTime createdAt;
    private final String title;
    private final String content;

    public NoticeRespDTO(Notice notice) {
        this.noticeId = notice.getNoticeId();
        this.createdAt = notice.getCreatedAt();
        this.title = notice.getTitle();
        this.content = notice.getContents();
    }
}
