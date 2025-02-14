package com.jandi.plan_backend.commu.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import java.time.LocalDateTime;

/**
 * 커뮤니티 게시글 정보를 저장하는 엔티티 클래스.
 * 이 클래스는 데이터베이스의 "community" 테이블과 매핑됨.
 *
 * 필드 설명:
 * - postId: 게시글의 고유 식별자. 데이터베이스에서 자동 증가되는 기본키임.
 * - user: 게시글 작성자 정보. User 엔티티와 다대일 관계를 가짐.
 * - createdAt: 게시글이 작성된 시각. null 값은 허용되지 않음.
 * - title: 게시글 제목. 최대 길이는 255자로 제한됨.
 * - contents: 게시글 본문 내용. TEXT 타입으로 저장되어 길이 제한이 없음.
 * - likeCount: 게시글에 달린 좋아요 수. null 값은 허용되지 않음.
 */
@Entity
@Table(name = "community")
@Data
public class Community {

    // 게시글의 고유 ID. 데이터베이스에서 자동으로 생성됨.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId;

    // 게시글 작성자 정보. 여러 게시글이 하나의 사용자에 속하므로 다대일 관계로 매핑됨.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 게시글 작성 시각을 나타냄. null 값은 허용되지 않음.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 게시글 제목. 최대 255자까지 저장할 수 있음.
    @Column(length = 255)
    private String title;

    // 게시글 내용. 길이 제한 없이 TEXT 타입으로 저장됨.
    @Column(columnDefinition = "TEXT")
    private String contents;

    // 게시글의 좋아요 수. null 값은 허용되지 않음.
    @Column(nullable = false)
    private Integer likeCount;

    @Column(nullable = false)
    private Integer commentCount; //댓글 수
}
