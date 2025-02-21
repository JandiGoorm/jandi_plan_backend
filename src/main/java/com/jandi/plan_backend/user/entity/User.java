package com.jandi.plan_backend.user.entity;

import com.jandi.plan_backend.commu.entity.Comment;
import com.jandi.plan_backend.commu.entity.Community;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 정보를 담는 엔티티.
 * 데이터베이스의 "users" 테이블과 매핑됨.
 */
@Entity
@Table(name = "users")
@Data
public class User {

    /**
     * 사용자 식별용 기본 키.
     * 데이터베이스가 자동으로 값을 생성함.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    /**
     * 사용자의 아이디.
     * 로그인 시 사용하며, 최대 50자까지 저장됨.
     */
    @Column(nullable = false, length = 50)
    private String userName;

    /**
     * 사용자의 이름.
     * 최대 50자까지 저장됨.
     */
    @Column(nullable = false, length = 50)
    private String firstName;

    /**
     * 사용자의 성.
     * 최대 50자까지 저장됨.
     */
    @Column(nullable = false, length = 50)
    private String lastName;

    /**
     * 사용자의 이메일 주소.
     * 유일해야 하며, 최대 100자까지 저장됨.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 사용자의 비밀번호.
     * 암호화된 형태로 저장.
     */
    @Column(nullable = false)
    private String password;

    /**
     * 사용자가 가입한 시각.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 사용자 정보가 마지막으로 수정된 시각.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 이메일 인증 여부.
     * true면 인증 완료, false면 미인증.
     */
    @Column(nullable = false)
    private Boolean verified;

    /**
     * 이메일 인증용 토큰.
     * 사용자가 이메일에 있는 링크를 클릭해 인증할 때 사용됨.
     */
    @Column
    private String verificationToken;

    /**
     * 이메일 인증 토큰의 만료 시각.
     * 토큰이 유효한 기간을 지정.
     */
    @Column
    private LocalDateTime tokenExpires;

    /**
     * 부적절 게시물 작성자 여부.
     * 기본값은 0. 값이 1일 때 부적절 유저로 간주하여 게시물 작성 제한
     */
    @Column
    private Boolean reported;

    /**
     * 해당 사용자가 작성한 커뮤니티 게시글 리스트.
     * 사용자가 탈퇴할 경우 연관 게시글도 함께 삭제되도록 CascadeType.ALL과 orphanRemoval을 적용.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Community> communities;
}
