package com.jandi.plan_backend.commu.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.jandi.plan_backend.user.entity.User;
import java.time.LocalDateTime;

/**
 * 신고된 게시글 정보를 저장하는 엔티티.
 * 이 클래스는 신고된 게시글에 대한 정보를 데이터베이스에 저장하기 위한 JPA 엔티티임.
 *
 * 필드 설명:
 * - reportId: 신고 ID. 기본키이며, 데이터베이스에서 자동으로 생성됨.
 * - community: 신고된 게시글 정보. Community 엔티티와 다대일 관계로 매핑됨.
 * - user: 신고를 한 사용자 정보. User 엔티티와 다대일 관계로 매핑됨.
 * - createdAt: 신고가 생성된 날짜와 시간.
 * - contents: 신고 사유나 상세 내용을 담는 필드. TEXT 타입으로 저장됨.
 */
@Entity
@Table(name = "reported")
@Data
public class Reported {

    // 신고 ID. 기본키로 사용하며, 값은 데이터베이스가 자동 생성함.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;

    // 신고된 게시글 정보.
    // Community 엔티티와 다대일 관계임.
    // "post_id" 컬럼으로 조인되며, null 값은 허용되지 않음.
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Community community;

    // 신고를 한 사용자 정보.
    // User 엔티티와 다대일 관계임.
    // "user_id" 컬럼으로 조인되며, null 값은 허용되지 않음.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 신고 생성 시각을 저장하는 필드.
    // null 값은 허용되지 않음.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 신고 내용이나 사유를 저장하는 필드.
    // 데이터베이스에 TEXT 타입으로 저장됨.
    @Column(columnDefinition = "TEXT")
    private String contents;
}
