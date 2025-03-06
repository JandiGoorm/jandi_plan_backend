package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RepliesDTO {
    private final Integer commentId;
    private final Integer parentCommentId;
    private final LocalDateTime createdAt;
    private final String contents;
    private final Integer likeCount;
    private final UserCommunityDTO user;
    private final boolean liked;

    public RepliesDTO(Comment comment, User user, ImageService imageService, boolean liked) {
        this.commentId = comment.getCommentId();
        this.parentCommentId = getParentCommentId();
        this.user = new UserCommunityDTO(user, imageService);
        this.createdAt = comment.getCreatedAt();
        this.contents = comment.getContents();
        this.likeCount = comment.getLikeCount();
        this.liked = liked;
    }
}
