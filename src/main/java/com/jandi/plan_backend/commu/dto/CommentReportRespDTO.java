package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.CommentReported;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CommentReportRespDTO {
    private final Integer reportId;
    private final Integer commentId;
    private final UserCommunityDTO user;
    private final LocalDateTime createdAt;
    private final String contents;

    public CommentReportRespDTO(CommentReported reported) {
        this.reportId = reported.getReportId();
        this.commentId = reported.getComment().getCommentId();
        this.user = new UserCommunityDTO(reported.getUser());
        this.createdAt = reported.getCreatedAt();
        this.contents = reported.getContents();
    }
}
