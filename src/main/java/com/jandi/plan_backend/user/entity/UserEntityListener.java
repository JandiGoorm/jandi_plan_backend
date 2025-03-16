package com.jandi.plan_backend.user.entity;

import com.jandi.plan_backend.user.repository.RoleLogRepository;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserEntityListener {

    private static RoleLogRepository roleLogRepository;

    @Autowired
    public void setRoleLogRepository(RoleLogRepository roleLogRepository) {
        UserEntityListener.roleLogRepository = roleLogRepository;
    }

    private int previousRole;

    @PreUpdate
    public void beforeUpdate(User user) {
        // 업데이트 이전의 역할 저장
        this.previousRole = user.getRole();
    }

    @PostUpdate
    public void afterUpdate(User user) {
        // 만약 Role이 변경되었다면 로그 저장
        if (this.previousRole != user.getRole()) {
            RoleLog roleLog = new RoleLog();
            roleLog.setUser(user);
            roleLog.setPrevRole(this.previousRole);
            roleLog.setNewRole(user.getRole());
            roleLog.setChangedBy("SYSTEM"); // 비정상적인 변경은 SYSTEM이 감지함

            roleLog.setChangedAt(LocalDateTime.now());
            roleLogRepository.save(roleLog);
        }
    }
}
