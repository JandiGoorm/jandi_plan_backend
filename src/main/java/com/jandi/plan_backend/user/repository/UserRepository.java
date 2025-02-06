package com.jandi.plan_backend.user.repository;

import com.jandi.plan_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

    boolean existsByEmail(String email);

    // 인증번호로 사용자 찾기
    Optional<User> findByEmailVerificationCode(String code);
}
