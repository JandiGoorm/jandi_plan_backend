package com.jandi.plan_backend.commu.community.dto;

import com.jandi.plan_backend.commu.community.entity.Community;
import com.jandi.plan_backend.image.service.ImageService;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 게시글 작성 시 클라이언트로 전달(응답)해줄 데이터를 담는 DTO
 * 게시글 및 작성자 정보를 넘겨준다
 */
@Data
public class CommunityRespDTO {
    private Integer postId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private String[] hashtag;
    private UserCommunityDTO user; // 유저 정보 중 민감 정보 제외

    public CommunityRespDTO(Community community, ImageService imageService) {
        this.postId = community.getPostId();
        this.user = new UserCommunityDTO(community.getUser(), imageService);
        this.createdAt = community.getCreatedAt();
        this.title = community.getTitle();
        this.content = community.getContents();
        this.likeCount = community.getLikeCount();
        this.viewCount = community.getViewCount();
        this.hashtag = community.getHashtags().toArray(new String[0]);
        this.commentCount = community.getCommentCount();
    }
}
