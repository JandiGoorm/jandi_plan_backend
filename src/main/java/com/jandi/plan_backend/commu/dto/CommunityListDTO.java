package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Community;
import com.jandi.plan_backend.storage.service.ImageService;
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

    public CommunityListDTO(Community community, ImageService imageService) {
        this.postId = community.getPostId();
        this.user = new UserCommunityDTO(community.getUser(), imageService);
        this.createdAt = community.getCreatedAt();
        this.title = community.getTitle();
        this.likeCount = community.getLikeCount();
        this.commentCount = community.getCommentCount();
    }

}
