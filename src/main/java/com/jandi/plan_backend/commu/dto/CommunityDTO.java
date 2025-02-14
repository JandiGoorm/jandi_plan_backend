package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Community;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 게시물 리스트 조회 시 가져올 게시물 관련 DTO
 */
@Getter
public class CommunityDTO {
    private final Integer postId;
    private final UserCommunityDTO user;
    private final LocalDateTime createdAt;
    private final String title;
    private final String contents;
    private final Integer likeCount;
    private final Integer commentCount;

    public CommunityDTO(Community community) {
        this.postId = community.getPostId();
        this.user = new UserCommunityDTO(community.getUser()); // UserDTO를 사용
        this.createdAt = community.getCreatedAt();
        this.title = community.getTitle();
        this.contents = community.getContents();
        this.likeCount = community.getLikeCount();
        this.commentCount = community.getCommentCount();
    }
}
