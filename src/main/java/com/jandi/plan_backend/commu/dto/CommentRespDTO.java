package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.image.service.ImageService;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentRespDTO {
    private final Integer commentId;
    private final UserCommunityDTO user;
    private final LocalDateTime createdAt;
    private final String contents;
    private final Integer likeCount;
    private final Integer repliesCount; // 답글 수

    // 프로필 필요한 버전
    public CommentRespDTO(Comment comment, ImageService imageService) {
        this.commentId = comment.getCommentId();
        this.user = new UserCommunityDTO(comment.getCommunity().getUser(), imageService);
        this.createdAt = comment.getCreatedAt();
        this.contents = comment.getContents();
        this.likeCount = comment.getLikeCount();
        this.repliesCount = comment.getRepliesCount();
    }

    // 프로필 필요 없는 버전
    public CommentRespDTO(Comment comment) {
        this.commentId = comment.getCommentId();
        this.user = new UserCommunityDTO(comment.getCommunity().getUser());
        this.createdAt = comment.getCreatedAt();
        this.contents = comment.getContents();
        this.likeCount = comment.getLikeCount();
        this.repliesCount = comment.getRepliesCount();
    }
}
