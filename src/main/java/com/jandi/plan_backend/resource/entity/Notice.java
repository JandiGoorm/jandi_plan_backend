package com.jandi.plan_backend.resource.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 공지사항 게시판 테이블 (notice)
 *
 * notice_id (PK)
 * created_at
 * title
 * contents
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
