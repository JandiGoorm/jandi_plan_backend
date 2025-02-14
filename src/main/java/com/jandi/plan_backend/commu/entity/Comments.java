package com.jandi.plan_backend.commu.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 커뮤니티 댓글 및 답글 정보를 저장하는 엔티티.
 * 이 엔티티는 "comments" 테이블에 매핑됨.
 *
 * 필드 설명:
 * - commentId: 댓글의 고유 식별자. 데이터베이스에서 자동 증가됨.
 * - community: 댓글이 속한 커뮤니티 게시글을 참조. Community 엔티티와 다대일 관계임.
 * - parentComment: 부모 댓글 정보를 참조함. 답글인 경우 상위 댓글을 저장하며, 최상위 댓글인 경우 null.
 * - userId: 댓글 작성자의 식별자(ID). User 엔티티 대신 단순 숫자 값으로 저장함.
 * - createdAt: 댓글이 작성된 시각.
 * - contents: 댓글의 내용. TEXT 타입으로 길이 제한 없이 저장됨.
 * - likeCount: 댓글에 달린 좋아요 수.
 */
@Entity
@Table(name = "comments")
@Data
public class Comments {

    // 댓글의 고유 ID. 자동 증가 전략 사용.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    // 댓글이 속한 커뮤니티 게시글. 다대일 관계 매핑.
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Community community;

    // 부모 댓글. 답글인 경우 상위 댓글을 참조하며, 최상위 댓글이면 null.
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comments parentComment;

    // 댓글 작성자의 ID. 단순 숫자 값으로 저장.
    @Column(nullable = false)
    private Integer userId;

    // 댓글 작성 시각.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 댓글 내용. 길이 제한 없이 TEXT 타입으로 저장.
    @Column(columnDefinition = "TEXT")
    private String contents;

    // 댓글에 달린 좋아요 수.
    @Column(nullable = false)
    private Integer likeCount;

    // 댓글에 달린 답글 수.
    @Column(nullable = false)
    private Integer repliesCount;
}
