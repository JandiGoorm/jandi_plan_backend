package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Comments;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class repliesDTO {
    private final Integer commentId;
    private final Integer userId;
    private final LocalDateTime createdAt;
    private final String contents;
    private final Integer likeCount;

    public repliesDTO(Comments comment) {
        this.commentId = comment.getCommentId();
        this.userId = comment.getUserId();
        this.createdAt = comment.getCreatedAt();
        this.contents = comment.getContents();
        this.likeCount = comment.getLikeCount();
    }
}
