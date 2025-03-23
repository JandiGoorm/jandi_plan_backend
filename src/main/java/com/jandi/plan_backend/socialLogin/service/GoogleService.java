package com.jandi.plan_backend.socialLogin.service;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.socialLogin.dto.GoogleTokenResponse;
import com.jandi.plan_backend.socialLogin.dto.GoogleUserInfo;
import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 구글 OAuth2 로그인 로직:
 *  1) 인가코드(code)로 구글 토큰 요청
 *  2) 구글 토큰으로 사용자 정보 조회
 *  3) DB 조회/가입
 *  4) JWT 발급 후 반환
 */
@Service
@RequiredArgsConstructor
public class GoogleService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * (1) code -> 구글 토큰 -> (2) 구글 유저정보 -> (3) DB 조회/가입 -> (4) JWT 발급
     */
    public AuthRespDTO googleLogin(String code) {
        // 1) code -> 구글 accessToken
        GoogleTokenResponse tokenResponse = requestGoogleToken(code);

        // 2) accessToken -> 구글 사용자 정보
        GoogleUserInfo googleUser = requestGoogleUserInfo(tokenResponse.getAccessToken());
        String googleId = googleUser.getSub();

        // 구글 계정에 이메일 동의가 없으면 "google_{id}@google" 사용
        String email = (googleUser.getEmail() != null)
                ? googleUser.getEmail()
                : ("google_" + googleId + "@google");

        // 3) DB에서 이메일로 User 조회 → 없으면 신규 생성 / 있으면 소셜 정보 갱신
        User user = findOrCreateUserByGoogle(email, googleId);

        // 4) JWT 발급
        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        return new AuthRespDTO(accessToken, refreshToken);
    }

    /* =================================================================
       1) 구글 토큰 요청
     ================================================================= */
    private GoogleTokenResponse requestGoogleToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        // 파라미터
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        // 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl, requestEntity, Map.class
        );

        // 응답 파싱
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("구글 토큰 발급 실패: " + response.getStatusCode());
        }

        Map body = response.getBody();
        String accessToken = (String) body.get("access_token");
        String refreshToken = (String) body.get("refresh_token");
        Integer expiresIn = (Integer) body.get("expires_in");

        return new GoogleTokenResponse(
                accessToken,
                refreshToken,
                (expiresIn != null) ? expiresIn : 0
        );
    }

    /* =================================================================
       2) 구글 사용자 정보 조회
     ================================================================= */
    private GoogleUserInfo requestGoogleUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        // 헤더에 accessToken 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("구글 사용자 정보 조회 실패: " + response.getStatusCode());
        }

        Map body = response.getBody();
        // 구글 OAuth2 v2: "id" 필드 / v3: "sub" 필드
        String sub = (String) body.get("id");
        String email = (String) body.get("email");

        return new GoogleUserInfo(sub, email);
    }

    /* =================================================================
       3) DB 조회/가입 로직
     ================================================================= */
    private User findOrCreateUserByGoogle(String email, String googleId) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            // 이미 해당 이메일이 존재
            User user = optionalUser.get();
            if (user.getSocialType() == null) {
                // 일반가입 + 아직 소셜연동 X
                user.setSocialType("GOOGLE");
                user.setSocialId(googleId);
                userRepository.save(user);
            } else if (!"GOOGLE".equals(user.getSocialType())) {
                // 이미 다른 소셜로 가입
                throw new RuntimeException("이미 다른 소셜("
                        + user.getSocialType() + ")로 가입된 이메일입니다.");
            } else {
                // 소셜타입이 GOOGLE인데, ID가 다르면 충돌
                if (!Objects.equals(user.getSocialId(), googleId)) {
                    throw new RuntimeException("이미 GOOGLE로 가입된 이메일이지만, ID가 달라 충돌합니다.");
                }
                // 같다면 -> 그대로 사용
            }
            return user;
        }

        // 해당 이메일이 전혀 없으면 새로 가입
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUserName("googleUser_" + googleId);
        newUser.setFirstName("Google");
        newUser.setLastName("User");
        newUser.setSocialType("GOOGLE");
        newUser.setSocialId(googleId);
        newUser.setVerified(true);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        newUser.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

        userRepository.save(newUser);
        return newUser;
    }
}
