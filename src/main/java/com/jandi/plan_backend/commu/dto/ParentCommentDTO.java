package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Comments;
import com.jandi.plan_backend.storage.service.ImageService;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ParentCommentDTO {
    private final Integer commentId;
    private final Integer userId;
    private final String profileImageUrl;
    private final LocalDateTime createdAt;
    private final String contents;
    private final Integer likeCount;
    private final Integer repliesCount; //답글 수

    public ParentCommentDTO(Comments comment, ImageService imageService) {
        this.commentId = comment.getCommentId();
        this.userId = comment.getUserId();
        this.profileImageUrl = imageService.getUserProfile(userId);
        this.createdAt = comment.getCreatedAt();
        this.contents = comment.getContents();
        this.likeCount = comment.getLikeCount();
        this.repliesCount = comment.getRepliesCount();
    }
}
