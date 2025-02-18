package com.jandi.plan_backend.resource.dto;

import lombok.Getter;

/**
 * 공지사항 작성 시 클라이언트로부터 전달되는 데이터를 담는 DTO
 * 공지사항 제목과 내용을 저장한다.
 */
@Getter
public class NoticeReqDTO {
    private String title;
    private String contents;

}
