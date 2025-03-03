package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.CommentReported;
import com.jandi.plan_backend.commu.entity.CommunityReported;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentReportRespDTO {
    private final Integer reportId;
    private final Integer commentId;
    private final UserCommunityDTO user;
    private final LocalDateTime createAt;
    private final String contents;

    public CommentReportRespDTO(CommentReported commentReported) {
        this.reportId = commentReported.getReportId();
        this.commentId = commentReported.getComment().getCommentId();
        this.user = new UserCommunityDTO(commentReported.getUser());
        this.createAt = commentReported.getCreatedAt();
        this.contents = commentReported.getContents();
    }
}
