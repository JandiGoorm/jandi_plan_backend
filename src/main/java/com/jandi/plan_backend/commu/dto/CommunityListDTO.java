package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.image.service.ImageService;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 게시물 리스트 조회 시 가져올 게시물 관련 DTO
 * 리스트로 조회 시 content는 필요없으므로 제외하고, 댓글 수를 포함한 나머지 정보를 전달한다
 */
@Getter
public class CommunityListDTO {
    private final Integer postId;
    private final UserCommunityDTO user;
    private final LocalDateTime createdAt;
    private final String title;
    private final Integer likeCount;
    private final Integer commentCount;
    private final Integer viewCount;
    private final String thumbnail;
    private final String preview;
    private final String[] hashtag;

    // 프로필 사진 필요한 버전
    public CommunityListDTO(Community community, ImageService imageService, String thumbnail) {
        this.postId = community.getPostId();
        this.viewCount = community.getViewCount();
        this.preview = community.getPreview();
        this.user = new UserCommunityDTO(community.getUser(), imageService);
        this.createdAt = community.getCreatedAt();
        this.title = community.getTitle();
        this.likeCount = community.getLikeCount();
        this.commentCount = community.getCommentCount();
        this.thumbnail = thumbnail;
        this.hashtag = community.getHashtags().toArray(new String[0]);
    }

    // 프로필 사진 필요없는 버전
    public CommunityListDTO(Community community) {
        this.postId = community.getPostId();
        this.viewCount = community.getViewCount();
        this.user = new UserCommunityDTO(community.getUser());
        this.createdAt = community.getCreatedAt();
        this.title = community.getTitle();
        this.likeCount = community.getLikeCount();
        this.commentCount = community.getCommentCount();
        this.preview = community.getPreview();
        this.hashtag = community.getHashtags().toArray(new String[0]);
        this.thumbnail = "";
    }
}
