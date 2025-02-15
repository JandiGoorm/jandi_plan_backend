package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Community;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityItemDTO {
    private final Integer postId;
    private final UserCommunityDTO user;
    private final LocalDateTime createdAt;
    private final String title;
    private final String content;
    private final Integer likeCount;
    private final Integer commentCount;

    public CommunityItemDTO(Community community) {
        this.postId = community.getPostId();
        this.user = new UserCommunityDTO(community.getUser()); // UserDTO를 사용
        this.createdAt = community.getCreatedAt();
        this.title = community.getTitle();
        this.content = community.getContents();
        this.likeCount = community.getLikeCount();
        this.commentCount = community.getCommentCount();
    }
}
