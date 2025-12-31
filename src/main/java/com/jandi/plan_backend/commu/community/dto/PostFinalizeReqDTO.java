package com.jandi.plan_backend.commu.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 임시 postId(음수 int) + 최종 게시글 정보
 */
@Data
public class PostFinalizeReqDTO {
    private int tempPostId;  // 음수 int

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 200, message = "제목은 1~200자여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 1, max = 10000, message = "내용은 1~10000자여야 합니다")
    private String content;

    @NotNull(message = "해시태그는 필수입니다")
    private List<String> hashtag;
}
