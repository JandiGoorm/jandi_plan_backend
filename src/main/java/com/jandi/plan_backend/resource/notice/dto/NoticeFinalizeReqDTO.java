package com.jandi.plan_backend.resource.notice.dto;

import lombok.Data;

/**
 * 공지사항 최종 작성 시 클라이언트로부터 전달받을 데이터 DTO
 * 임시 Notice ID, 제목, 내용 등을 포함함.
 */
@Data
public class NoticeFinalizeReqDTO {
    private int tempNoticeId;  // 음수 int, 임시 Notice ID
    private String title;
    private String content;
}
