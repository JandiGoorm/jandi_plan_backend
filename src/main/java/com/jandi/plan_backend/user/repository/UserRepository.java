package com.jandi.plan_backend.user.repository;

import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserId(Integer userId);
    /**
     * 주어진 이메일을 가진 User 엔티티를 Optional로 반환.
     * 이메일은 고유하다고 가정함.
     *
     * @param email 찾으려는 사용자의 이메일 주소
     * @return 해당 이메일을 가진 User가 있으면 Optional에 감싸서 반환, 없으면 빈 Optional 반환
     */
    Optional<User> findByEmail(String email);

    /**
     * 주어진 사용자명을 가진 User 엔티티를 Optional로 반환.
     *
     * @param userName 찾으려는 사용자의 사용자명
     * @return 해당 사용자명을 가진 User가 있으면 Optional에 감싸서 반환, 없으면 빈 Optional 반환
     */
    Optional<User> findByUserName(String userName);

    /**
     * 주어진 이메일이 이미 존재하는지 여부를 확인.
     *
     * @param email 확인할 이메일 주소
     * @return 해당 이메일이 존재하면 true, 존재하지 않으면 false 반환
     */
    boolean existsByEmail(String email);

    /**
     * 주어진 인증 토큰과 일치하는 User 엔티티를 Optional로 반환.
     * 이메일 인증 과정에서 사용됨.
     *
     * @param token 인증 토큰 문자열
     * @return 해당 토큰과 일치하는 User가 있으면 Optional에 감싸서 반환, 없으면 빈 Optional 반환
     */
    Optional<User> findByVerificationToken(String token);

    List<User> findByVerifiedFalseAndTokenExpiresBefore(LocalDateTime dateTime);

    // 부적절 유저 조회용
    Integer countByReportedIsTrue();
    Page<User> findByReportedIsTrue(Pageable pageable);

    // 지난 7일간 신규 가입된 유저 수 반환
    long countByCreatedAtBetween(LocalDateTime today, LocalDateTime last7Days);
}
