package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.entity.RoleLog;
import com.jandi.plan_backend.user.repository.RoleLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleMonitorService {

    private final RoleLogRepository roleLogRepository;

    /**
     * 1시간마다 Role 변경 로그를 감시하고, 비정상적인 변경이 있는 경우 경고 로그를 출력합니다.
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void monitorRoleChanges() {
        log.info("Role 변경 로그 감시 시작...");

        List<RoleLog> suspiciousLogs = roleLogRepository.findAll().stream()
                .filter(log -> "SYSTEM".equals(log.getChangedBy()))
                .toList();

        if (!suspiciousLogs.isEmpty()) {
            log.warn("비정상적인 Role 변경 감지");
            for (RoleLog logEntry : suspiciousLogs) {
                log.warn("[UserID: {}] role {} → {} 으로 변경됨",
                        logEntry.getUser().getUserId(),
                        logEntry.getPrevRole(),
                        logEntry.getNewRole());
            }
        } else {
            log.info("비정상적인 Role 변경 없음");
        }
    }
}

