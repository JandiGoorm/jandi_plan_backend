package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.dto.*;
import com.jandi.plan_backend.user.service.UserService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserController는 회원가입, 로그인, 이메일 인증, 비밀번호 찾기,
 * 사용자 정보 조회, 비밀번호 변경, 그리고 리프레시 토큰 갱신 기능을 제공하는 REST 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("회원가입 완료! 이메일의 링크를 클릭하면 인증이 완료됨");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("로그인 시도, 이메일: {}", userLoginDTO.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword())
        );
        AuthResponse authResponse = userService.login(userLoginDTO);
        log.info("로그인 성공, 이메일: {}, JWT 토큰 생성됨", userLoginDTO.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * 리프레시 토큰 갱신 엔드포인트.
     * 클라이언트가 보낸 리프레시 토큰을 검증하고, 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     *
     * @param request JSON 형식의 요청 본문 (키: "refreshToken")
     * @return 새로 발급된 토큰들을 포함한 AuthResponse 객체
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            AuthResponse authResponse = userService.refreshToken(refreshToken);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "리프레시 토큰이 유효하지 않거나 만료되었습니다."));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyByToken(@RequestParam("token") String token) {
        boolean result = userService.verifyEmailByToken(token);
        if (result) {
            return ResponseEntity.ok("이메일 인증 완료됨");
        }
        return ResponseEntity.badRequest().body("인증 실패, 토큰이 유효하지 않거나 만료됨");
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.forgotPassword(email);
        return ResponseEntity.ok("임시 비밀번호 발급됨, 이메일 확인");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal com.jandi.plan_backend.security.CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Integer userId = customUserDetails.getUserId();
        UserInfoResponseDto dto = userService.getUserInfo(userId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal com.jandi.plan_backend.security.CustomUserDetails customUserDetails,
            @RequestBody ChangePasswordDTO dto) {
        if (customUserDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        String email = customUserDetails.getUsername();
        try {
            userService.changePassword(email, dto);
            return ResponseEntity.ok(Map.of("message", "비밀번호 변경이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
