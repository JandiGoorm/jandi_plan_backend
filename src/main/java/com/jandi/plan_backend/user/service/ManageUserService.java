package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.user.dto.UserListDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ManageUserService {
    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;
    private final UserService userService;


    public ManageUserService(ValidationUtil validationUtil, UserRepository userRepository, UserService userService) {
        this.validationUtil = validationUtil;
        this.userRepository = userRepository;
        this.userService = userService;
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
}
