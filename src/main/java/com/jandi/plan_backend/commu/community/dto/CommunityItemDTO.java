package com.jandi.plan_backend.commu.community.dto;

import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.image.service.ImageService;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 특정 게시글 정보 조회 시 가져올 게시물 관련 DTO
 * 게시글만 조회 시 댓글 수를 제외하고, 내용을 포함한 전반적인 정보를 전달한다
 */
@Getter
public class CommunityItemDTO {
    private final Integer postId;
    private final UserCommunityDTO user;
    private final LocalDateTime createdAt;
    private final String title;
    private final String content;
    private final Integer likeCount;
    private final Integer commentCount;
    private final Integer viewCount;
    private final Boolean liked;
    private final List<String> hashtag;

    public CommunityItemDTO(Community community, ImageService imageService, boolean liked) {
        this.postId = community.getPostId();
        this.viewCount = community.getViewCount();
        this.user = new UserCommunityDTO(community.getUser(), imageService);
        this.createdAt = community.getCreatedAt();
        this.title = community.getTitle();
        this.content = community.getContents();
        this.likeCount = community.getLikeCount();
        this.commentCount = community.getCommentCount();
        this.liked = liked;
        this.hashtag = community.getHashtags();
    }
}
