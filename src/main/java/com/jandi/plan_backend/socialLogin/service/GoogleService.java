package com.jandi.plan_backend.socialLogin.service;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.socialLogin.dto.GoogleTokenResponse;
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
import java.util.Optional;
import java.util.UUID;

/**
 * 구글 서버와 통신(토큰/유저정보) + DB조회/가입/로그인(JWT발급) 로직
 */
@Service
@RequiredArgsConstructor
public class GoogleService {

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * (1) code -> 구글 토큰 -> (2) 구글 유저정보 -> (3) DB 조회/가입 -> (4) JWT 발급
     */
    public AuthRespDTO googleLogin(String code) {
        // 1) code -> 구글 access_token
        GoogleTokenResponse tokenResponse = getGoogleToken(code);

        // 2) access_token -> 구글 사용자 정보
        com.jandi.plan_backend.socialLogin.service.GoogleUserInfo userInfo = getGoogleUserInfo(tokenResponse.getAccessToken());
        String googleId = userInfo.getSub(); // 구글에서 userinfo 응답의 "id" 또는 "sub" 사용

        // 3) DB 조회 (socialType="GOOGLE", socialId=googleId)
        Optional<User> optionalUser = userRepository.findBySocialTypeAndSocialId("GOOGLE", googleId);
        User user;
        if (optionalUser.isEmpty()) {
            // (A) 소셜 가입
            user = new User();

            // 구글 계정에 이메일 동의 항목이 꺼져있으면 email이 null일 수 있음
            String email = (userInfo.getEmail() != null) ? userInfo.getEmail() : ("google_" + googleId + "@google");
            user.setEmail(email);

            // userName, firstName, lastName 등 필수 컬럼
            user.setUserName("googleUser_" + googleId);
            user.setFirstName("Google");
            user.setLastName("User");

            user.setSocialType("GOOGLE");
            user.setSocialId(googleId);
            user.setVerified(true);  // 소셜로그인 시 별도의 이메일 인증 없이 verified=true
            // 비밀번호는 임의로 생성
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            userRepository.save(user);
        } else {
            // (B) 기존 유저
            user = optionalUser.get();
            // 필요 시 이메일/닉네임 업데이트 등
        }

        // 4) JWT 발급
        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        return new AuthRespDTO(accessToken, refreshToken);
    }

    /**
     * (실제 구현) 구글에 토큰 요청
     * - endpoint: https://oauth2.googleapis.com/token
     */
    private GoogleTokenResponse getGoogleToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        // 파라미터
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("code", code);

        // 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl,
                new HttpEntity<>(params, headers),
                Map.class
        );

        // 응답 파싱
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            String accessToken = (String) body.get("access_token");
            String refreshToken = (String) body.get("refresh_token");
            Integer expiresIn = (Integer) body.get("expires_in");

            return new GoogleTokenResponse(
                    accessToken,
                    refreshToken,
                    (expiresIn != null) ? expiresIn : 0
            );
        } else {
            throw new RuntimeException("구글 토큰 발급 실패: " + response.getStatusCode());
        }
    }

    /**
     * (실제 구현) 구글 사용자 정보 가져오기
     * - endpoint: https://www.googleapis.com/oauth2/v2/userinfo
     *   (또는 https://www.googleapis.com/oauth2/v3/userinfo)
     */
    private com.jandi.plan_backend.socialLogin.service.GoogleUserInfo getGoogleUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            // 구글에서는 v2/userinfo에서 "id" 키를 사용, OIDC 기반이면 "sub" 키를 쓸 수도 있음
            String sub = (String) body.get("id");
            String email = (String) body.get("email");

            return new com.jandi.plan_backend.socialLogin.service.GoogleUserInfo(sub, email);
        } else {
            throw new RuntimeException("구글 사용자 정보 조회 실패: " + response.getStatusCode());
        }
    }
}
