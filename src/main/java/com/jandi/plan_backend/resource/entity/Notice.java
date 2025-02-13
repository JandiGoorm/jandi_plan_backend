package com.jandi.plan_backend.resource.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 공지사항 게시판 테이블(Notice)을 나타내는 엔티티.
 *
 * 이 클래스는 데이터베이스의 "notice" 테이블과 매핑됨.
 *
 * 필드 설명:
 * - noticeId:
 *   - 공지사항의 고유 식별자(PK).
 *   - 자동 증가(IDENTITY) 전략을 사용해 값이 생성됨.
 *
 * - createdAt:
 *   - 공지사항이 생성된 날짜와 시간을 저장.
 *   - null 값 허용하지 않음.
 *
 * - title:
 *   - 공지사항의 제목을 저장.
 *   - 최대 길이 255자로 제한됨.
 *
 * - contents:
 *   - 공지사항의 내용을 저장.
 *   - TEXT 타입으로 지정되어 길이 제한이 없음.
 *
 * Lombok의 @Data 어노테이션을 사용해서 getter, setter, toString, equals, hashCode 메서드가 자동 생성됨.
 */
@Entity
@Table(name = "notice")
@Data
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer noticeId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;
}
