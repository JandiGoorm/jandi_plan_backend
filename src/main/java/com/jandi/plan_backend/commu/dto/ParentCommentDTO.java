package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.entity.User;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ParentCommentDTO {
    private final Integer commentId;
    private final UserCommunityDTO user;
    private final LocalDateTime createdAt;
    private final String contents;
    private final Integer likeCount;
    private final Integer repliesCount;
    private final boolean liked;

    public ParentCommentDTO(Comment comment, User commentAuthor, ImageService imageService, boolean liked) {
        this.commentId = comment.getCommentId();
        this.user = new UserCommunityDTO(commentAuthor, imageService);
        this.createdAt = comment.getCreatedAt();
        this.contents = comment.getContents();
        this.likeCount = comment.getLikeCount();
        this.repliesCount = comment.getRepliesCount();
        this.liked = liked;
    }
}
