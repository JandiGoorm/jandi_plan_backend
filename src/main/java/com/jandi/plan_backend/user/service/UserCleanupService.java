package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
public class UserCleanupService {

    private final UserRepository userRepository;

    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 매 시간 정각마다 미인증 사용자(verified == false) 중,
     * 토큰 만료 시간이 현재 시간보다 이전인 사용자를 삭제합니다.
     */
    @Scheduled(cron = "0 0 * * * *")  // 매 정각 실행 (원하는 주기로 수정 가능)
    @Transactional
    public void cleanupUnverifiedUsers() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        List<User> unverifiedUsers = userRepository.findByVerifiedFalseAndTokenExpiresBefore(now);
        if (!unverifiedUsers.isEmpty()) {
            log.info("삭제할 미인증 사용자 {}명을 찾았습니다.", unverifiedUsers.size());
            unverifiedUsers.forEach(user -> {
                log.info("미인증 사용자 삭제: userId={}, email={}", user.getUserId(), user.getEmail());
                userRepository.delete(user);
            });
        }
    }
}
