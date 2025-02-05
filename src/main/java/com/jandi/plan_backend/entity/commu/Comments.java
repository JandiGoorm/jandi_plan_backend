package com.jandi.plan_backend.entity.commu;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 커뮤니티 댓글(및 답글) 테이블 (comments)
 *
 * comment_id (PK)
 * post_id (FK → community.post_id)
 * parent_comment_id (FK → comments.comment_id, nullable)
 * user_id (댓글 작성자)
 * created_at
 * contents
 * like_count
 */
@Entity
@Table(name = "comments")
@Data
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Community community;

    // 자기 참조 (답글인 경우)
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comments parentComment;

    // 댓글 작성자 (간단히 userId만 저장하는 경우)
    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(nullable = false)
    private Integer likeCount;
}
