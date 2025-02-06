package com.jandi.plan_backend.commu.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;

import java.time.LocalDateTime;

/**
 * 커뮤니티 게시글 테이블 (community)
 *
 * post_id (PK)
 * user_id (FK)
 * created_at
 * title
 * contents
 * like_count
 */
@Entity
@Table(name = "community")
@Data
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(nullable = false)
    private Integer likeCount;
}
