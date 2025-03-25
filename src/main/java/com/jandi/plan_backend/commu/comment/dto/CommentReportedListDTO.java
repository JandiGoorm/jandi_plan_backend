package com.jandi.plan_backend.commu.comment.dto;

import com.jandi.plan_backend.commu.comment.entity.Comment;
import lombok.Getter;

/** 관리자 페이지에서 신고된 댓글 목록을 조회할 때 응답을 위한 DTO */
@Getter
public class CommentReportedListDTO extends CommentRespDTO{
    private final Integer reportCount;

    public CommentReportedListDTO(Comment comment, Integer reportCount) {
        super(comment);
        this.reportCount = reportCount;
    }
}
