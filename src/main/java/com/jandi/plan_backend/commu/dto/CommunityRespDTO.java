package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Community;
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
    private UserCommunityDTO user; // 유저 정보 중 민감 정보 제외

    public CommunityRespDTO(Community community) {
        this.postId = community.getPostId();
        this.title = community.getTitle();
        this.content = community.getContents();
        this.createdAt = community.getCreatedAt();
        this.likeCount = community.getLikeCount();
        this.commentCount = community.getCommentCount();
        this.user = new UserCommunityDTO(community.getUser());
    }
}
