package com.jandi.plan_backend.commu.dto;

import lombok.Data;

/**
 * 게시글 작성 시 클라이언트로부터 전달되는 데이터를 담는 DTO
 * 게시글 제목과 내용을 저장한다
 */
@Data
public class CommunityWritePostDTO {
    private final String title;
    private final String content;
}
