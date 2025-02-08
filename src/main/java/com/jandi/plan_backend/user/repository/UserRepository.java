package com.jandi.plan_backend.user.repository;

import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // userName(혹은 닉네임)으로 사용자 조회
    Optional<User> findByUserName(String userName);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 인증 토큰으로 사용자 조회
    Optional<User> findByVerificationToken(String token);
}
