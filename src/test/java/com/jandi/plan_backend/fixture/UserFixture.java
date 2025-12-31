package com.jandi.plan_backend.fixture;

import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.TimeUtil;

import java.time.LocalDateTime;

/**
 * User 관련 테스트 데이터 팩토리 클래스
 */
public class UserFixture {

    private static final LocalDateTime NOW = TimeUtil.now();

    /**
     * 일반 사용자 생성
     */
    public static User createNormalUser() {
        User user = new User();
        user.setUserId(1);
        user.setEmail("user@example.com");
        user.setUserName("normalUser");
        user.setFirstName("홍");
        user.setLastName("길동");
        user.setPassword("encodedPassword");
        user.setVerified(true);
        user.setReported(false);
        user.setRole(0); // USER
        user.setCreatedAt(NOW);
        user.setUpdatedAt(NOW);
        return user;
    }

    /**
     * 관리자 사용자 생성
     */
    public static User createAdminUser() {
        User user = createNormalUser();
        user.setUserId(2);
        user.setEmail("admin@example.com");
        user.setUserName("adminUser");
        user.setRole(2); // ADMIN
        return user;
    }

    /**
     * 스태프 사용자 생성
     */
    public static User createStaffUser() {
        User user = createNormalUser();
        user.setUserId(3);
        user.setEmail("staff@example.com");
        user.setUserName("staffUser");
        user.setRole(1); // STAFF
        return user;
    }

    /**
     * 제한된 사용자 생성 (신고된 사용자)
     */
    public static User createRestrictedUser() {
        User user = createNormalUser();
        user.setUserId(4);
        user.setEmail("restricted@example.com");
        user.setUserName("restrictedUser");
        user.setReported(true);
        return user;
    }

    /**
     * 이메일 미인증 사용자 생성
     */
    public static User createUnverifiedUser() {
        User user = createNormalUser();
        user.setUserId(5);
        user.setEmail("unverified@example.com");
        user.setUserName("unverifiedUser");
        user.setVerified(false);
        user.setVerificationToken("test-token-12345");
        user.setTokenExpires(NOW.plusHours(24));
        return user;
    }

    /**
     * 회원가입 요청 DTO 생성
     */
    public static UserRegisterDTO createValidRegisterDTO() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("newuser@example.com");
        dto.setPassword("password123!");
        dto.setUserName("newUser");
        dto.setFirstName("김");
        dto.setLastName("철수");
        return dto;
    }

    /**
     * 로그인 요청 DTO 생성
     */
    public static UserLoginDTO createValidLoginDTO() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("user@example.com");
        dto.setPassword("password123!");
        return dto;
    }

    /**
     * 특정 ID와 이메일을 가진 사용자 생성
     */
    public static User createUserWithIdAndEmail(Integer id, String email) {
        User user = createNormalUser();
        user.setUserId(id);
        user.setEmail(email);
        user.setUserName("user" + id);
        return user;
    }
}
