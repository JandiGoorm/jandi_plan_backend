package com.jandi.plan_backend.user.controller;

import com.jandi.plan_backend.user.dto.*;
import com.jandi.plan_backend.user.service.UserService;
import com.jandi.plan_backend.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    // 사용자 관련 비즈니스 로직 처리 객체
    private final UserService userService;
    // 인증 처리 담당 객체 (스프링 시큐리티에서 사용자 인증 시 사용)
    private final AuthenticationManager authenticationManager;
    // JWT 토큰 생성 및 검증 기능 제공 객체
    private final JwtTokenProvider jwtTokenProvider;

    // 필요한 의존성을 생성자 주입 방식으로 받음
    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 회원가입 엔드포인트.
     * 요청 본문으로 전달된 UserRegisterDTO를 이용해 사용자 등록 처리 후,
     * 인증 링크가 포함된 이메일을 발송한다.
     *
     * @param dto 회원가입에 필요한 정보를 담은 DTO 객체
     * @return 회원가입 완료 메시지 응답
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        // 회원가입 로직 수행(사용자 등록 및 인증 이메일 발송)
        userService.registerUser(dto);
        // 성공 메시지 반환
        return ResponseEntity.ok("회원가입 완료! 이메일의 링크를 클릭하면 인증이 완료됨");
    }

    /**
     * 로그인 엔드포인트.
     * 요청 본문으로 전달된 UserLoginDTO를 이용해 인증을 시도하고,
     * 성공 시 JWT 토큰을 발급하여 반환한다.
     *
     * @param userLoginDTO 로그인에 필요한 이메일과 비밀번호가 담긴 DTO 객체
     * @return JWT 토큰을 포함한 AuthResponse 객체
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginDTO userLoginDTO) {
        // 로그인 시도 로그 출력
        log.info("로그인 시도, 이메일: {}", userLoginDTO.getEmail());
        // 이메일과 비밀번호로 UsernamePasswordAuthenticationToken 생성 후, 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword())
        );
        // 인증 성공 시 JWT 토큰 생성
        String token = jwtTokenProvider.createToken(userLoginDTO.getEmail());
        log.info("로그인 성공, 이메일: {}, JWT 토큰 생성됨", userLoginDTO.getEmail());
        // JWT 토큰을 AuthResponse 객체로 감싸서 응답 반환
        return ResponseEntity.ok(new AuthResponse(token));
    }

    /**
     * 이메일 인증 엔드포인트.
     * 요청 파라미터로 전달된 토큰을 이용해 이메일 인증을 진행한다.
     *
     * @param token 이메일 인증을 위한 토큰
     * @return 인증 성공 또는 실패에 대한 메시지 응답
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyByToken(@RequestParam("token") String token) {
        // 토큰을 이용해 이메일 인증 처리
        boolean result = userService.verifyEmailByToken(token);
        // 인증 성공 시 성공 메시지 반환, 실패 시 400 에러 반환
        if (result) {
            return ResponseEntity.ok("이메일 인증 완료됨");
        }
        return ResponseEntity.badRequest().body("인증 실패, 토큰이 유효하지 않거나 만료됨");
    }

    /**
     * 비밀번호 찾기(임시 비밀번호 발급) 엔드포인트.
     * 요청 본문에 포함된 이메일 주소로 임시 비밀번호를 발급하고,
     * 해당 이메일로 임시 비밀번호를 발송한다.
     *
     * @param request Map 형태로 전달된 이메일 정보 (키: "email")
     * @return 임시 비밀번호 발급 완료 메시지 응답
     */
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        // 요청 맵에서 "email" 키로 이메일 값을 추출
        String email = request.get("email");
        // 비밀번호 찾기 로직 수행(임시 비밀번호 발급 및 이메일 발송)
        userService.forgotPassword(email);
        // 성공 메시지 반환
        return ResponseEntity.ok("임시 비밀번호 발급됨, 이메일 확인");
    }
}
