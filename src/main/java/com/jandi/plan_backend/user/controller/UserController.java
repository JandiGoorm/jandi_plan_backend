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
 * 사용자 정보 조회 및 비밀번호 변경 등의 기능을 제공하는 REST 컨트롤러입니다.
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

    /**
     * 회원가입 엔드포인트.
     * 클라이언트가 전송한 UserRegisterDTO를 이용해 회원가입을 처리하고,
     * 인증 이메일 발송 후 성공 메시지를 반환합니다.
     *
     * @param dto 회원가입에 필요한 정보 (이메일, 사용자 이름, 비밀번호 등)
     * @return 회원가입 완료 메시지
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok("회원가입 완료! 이메일의 링크를 클릭하면 인증이 완료됨");
    }

    /**
     * 로그인 엔드포인트.
     * 클라이언트로부터 전송된 UserLoginDTO를 이용하여 사용자 인증을 시도하고,
     * 인증에 성공하면 JWT 토큰을 발급하여 응답으로 반환합니다.
     *
     * @param userLoginDTO 로그인에 필요한 이메일과 비밀번호 정보를 담은 DTO
     * @return JWT 토큰을 포함한 AuthResponse 객체
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("로그인 시도, 이메일: {}", userLoginDTO.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword())
        );
        String token = jwtTokenProvider.createToken(userLoginDTO.getEmail());
        log.info("로그인 성공, 이메일: {}, JWT 토큰 생성됨", userLoginDTO.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    /**
     * 이메일 인증 엔드포인트.
     * 요청 파라미터로 전달된 인증 토큰을 이용해 이메일 인증을 처리합니다.
     *
     * @param token 이메일 인증을 위한 토큰
     * @return 인증 성공 또는 실패 메시지
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyByToken(@RequestParam("token") String token) {
        boolean result = userService.verifyEmailByToken(token);
        if (result) {
            return ResponseEntity.ok("이메일 인증 완료됨");
        }
        return ResponseEntity.badRequest().body("인증 실패, 토큰이 유효하지 않거나 만료됨");
    }

    /**
     * 비밀번호 찾기(임시 비밀번호 발급) 엔드포인트.
     * 요청 본문에 포함된 이메일 주소로 임시 비밀번호를 발급하고,
     * 해당 이메일로 임시 비밀번호 안내 메일을 전송합니다.
     *
     * @param request Map 형식으로 전달된 이메일 정보 (키: "email")
     * @return 임시 비밀번호 발급 완료 메시지
     */
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.forgotPassword(email);
        return ResponseEntity.ok("임시 비밀번호 발급됨, 이메일 확인");
    }

    /**
     * 인증된 사용자의 상세 정보를 조회하는 엔드포인트.
     * JWT 토큰을 통해 인증된 사용자 정보를 기반으로 해당 사용자의 프로필 정보를 반환합니다.
     * 반환되는 정보에는 이메일, 퍼스트네임, 라스트네임, 생성일, 업데이트일, 유저네임,
     * 인증 여부, 신고 여부, 그리고 프로필 사진의 공개 URL이 포함됩니다.
     *
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @return 사용자 상세 정보를 담은 UserInfoResponseDto (JSON 형식)
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal com.jandi.plan_backend.security.CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        Integer userId = customUserDetails.getUserId();
        UserInfoResponseDto dto = userService.getUserInfo(userId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 비밀번호 변경 엔드포인트.
     * 인증된 사용자가 현재 비밀번호와 새 비밀번호를 제공하면,
     * 현재 비밀번호를 확인 후 새 비밀번호로 업데이트합니다.
     *
     * @param customUserDetails 인증된 사용자 정보 (CustomUserDetails)
     * @param dto ChangePasswordDTO (현재 비밀번호, 새 비밀번호)
     * @return 변경 완료 메시지 또는 오류 메시지
     */
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
