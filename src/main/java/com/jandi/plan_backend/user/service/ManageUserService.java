package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.commu.dto.UserListDTO;
import com.jandi.plan_backend.user.dto.RoleReqDTO;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.jandi.plan_backend.user.entity.RoleLog;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.RoleLogRepository;
import jakarta.transaction.Transactional;
import com.jandi.plan_backend.user.entity.Role;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

@Slf4j
@Service
public class ManageUserService {
    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleLogRepository roleLogRepository;


    public ManageUserService(
            ValidationUtil validationUtil,
            UserRepository userRepository,
            UserService userService,
            RoleLogRepository roleLogRepository
    ) {
        this.validationUtil = validationUtil;
        this.userRepository = userRepository;
        this.userService = userService;
        this.roleLogRepository = roleLogRepository;
    }

    //유저 목록 로드
    public Page<UserListDTO> getAllUsers(String userEmail, int page, int size) {
        User admin = validationUtil.validateUserExists(userEmail);

        long totalCount = userRepository.count();
        return PaginationService.getPagedData(page, size, totalCount,
                userRepository::findAll, UserListDTO::new);
    }

    //부적절 유저 목록 로드
    public Page<UserListDTO> getRestrictedUsers(String userEmail, int page, int size) {
        User admin = validationUtil.validateUserExists(userEmail);

        long totalCount = userRepository.countByReportedIsTrue();
        return PaginationService.getPagedData(page, size, totalCount,
                userRepository::findByReportedIsTrue, UserListDTO::new);
    }

    //유저 제재하기
    public Boolean permitUser(String userEmail, Integer userId) {
        User admin = validationUtil.validateUserExists(userEmail);

        //유저 찾기
        User user = userRepository.findByUserId(userId).orElse(null);
        if(user==null) { //사용자 미존재 시 오류 반환
            throw new BadRequestExceptionMessage("사용자가 존재하지 않습니다");
        }
        log.info("선택된 유저: {}{}", user.getEmail(), user.getReported() ? "(제재)" : "(일반)");

        //유저 제재/제한 해제 후 제한 여부 반환
        user.setReported(!user.getReported());
        userRepository.save(user);
        return user.getReported();
    }

    // 유저 강제 탈퇴
    public Boolean withdrawUser(String userEmail, Integer userId) {
        User admin = validationUtil.validateUserExists(userEmail);

        // 유저 찾기
        User user = userRepository.findByUserId(userId).orElse(null);
        if(user==null) { //사용자 미존재 시 오류 반환
            throw new BadRequestExceptionMessage("사용자가 존재하지 않습니다");
        }

        log.info("선택된 유저: {}{}", user.getEmail(), user.getReported() ? "(제재)" : "(일반)");
        return userService.deleteUser(user.getEmail());
    }

    // 유저 권한 변경
    @Transactional
    public void changeUserRole(Integer targetUserId, RoleReqDTO reqDTO, String curUserEmail) {
        try{
            // 대상 사용자 검증
            User targetUser = validationUtil.validateUserExists(targetUserId);
            User curUser = validationUtil.validateUserExists(curUserEmail);

            // 기존 역할 저장
            int previousRole = targetUser.getRole(); // 기존 역할
            int intRole = Role.fromString(reqDTO.getRoleName()); // 새 역할: 문자열 -> 숫자로 변환

            // 기존과 같을 경우 변경하지 않고 에러 반환
            if(previousRole==intRole) {
                throw new BadRequestExceptionMessage("기존과 동일한 권한을 입력했습니다.");
            }

            // 역할 변경
            targetUser.setRole(intRole);
            userRepository.save(targetUser);

            // 로그 기록
            RoleLog roleLog = new RoleLog();
            roleLog.setUser(targetUser);
            roleLog.setPrevRole(previousRole);
            roleLog.setNewRole(intRole);
            roleLog.setChangedBy(curUser.getEmail());
            roleLog.setChangedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            roleLogRepository.save(roleLog);
        }catch(Exception e){
            throw new BadRequestExceptionMessage(e.getMessage());
        }
    }
}
