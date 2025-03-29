package com.jandi.plan_backend.commu.community.dto;

import lombok.Data;

import java.util.List;

/**
 * 임시 postId(음수 int) + 최종 게시글 정보
 */
@Data
public class PostFinalizeReqDTO {
    private int tempPostId;  // 음수 int
    private String title;
    private String content;
    private List<String> hashtag;
}
