package com.jandi.plan_backend.resource.dto;

import com.jandi.plan_backend.resource.entity.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeWriteRespDTO {
    private final Integer noticeId;
    private final LocalDateTime createdAt;
    private final String title;
    private final String contents;

    public NoticeWriteRespDTO(Notice notice) {
        this.noticeId = notice.getNoticeId();
        this.createdAt = notice.getCreatedAt();
        this.title = notice.getTitle();
        this.contents = notice.getContents();
    }
}
