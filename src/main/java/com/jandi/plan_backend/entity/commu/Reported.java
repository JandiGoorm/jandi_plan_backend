package com.jandi.plan_backend.entity.commu;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.entity.user.User;

import java.time.LocalDateTime;

/**
 * 신고된 게시글 정보 테이블 (reported)
 *
 * report_id (PK)
 * post_id (FK → community.post_id)
 * user_id (FK → user.user_id)
 * created_at
 * contents (신고 사유)
 */
@Entity
@Table(name = "reported")
@Data
public class Reported {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Community community;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String contents;
}
