package com.jandi.plan_backend.socialLogin.service;

import com.jandi.plan_backend.socialLogin.dto.KakaoTokenResponse;
import com.jandi.plan_backend.socialLogin.dto.KakaoUserInfo;
import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.security.JwtTokenProvider;
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
 * 카카오 서버와 통신(토큰/유저정보) + DB조회/가입/로그인(JWT발급) 로직
 */
@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 인가 코드 -> 카카오 토큰 -> 카카오 유저정보 -> DB조회/가입 -> JWT 발급
     */
    public AuthRespDTO kakaoLogin(String code) {
        // 1) code -> 카카오 access_token
        KakaoTokenResponse tokenResponse = getKakaoToken(code);

        // 2) access_token -> 카카오 사용자 정보
        KakaoUserInfo userInfo = getKakaoUserInfo(tokenResponse.getAccessToken());
        String kakaoId = userInfo.getId();

        // 3) DB 조회 (소셜타입 = "KAKAO", socialId = kakaoId)
        Optional<User> optionalUser = userRepository.findBySocialTypeAndSocialId("KAKAO", kakaoId);
        User user;
        if (optionalUser.isEmpty()) {
            // (A) 소셜 가입
            user = new User();

            // 카카오 이메일 동의항목이 꺼져있으면 null일 수 있으므로 임시 이메일 구성
            String email = (userInfo.getEmail() != null) ? userInfo.getEmail()
                    : "kakao_" + kakaoId + "@kakao";
            user.setEmail(email);

            // userName, firstName, lastName 등 필수 칼럼
            user.setUserName("kakaoUser_" + kakaoId);
            user.setFirstName("Kakao");  // DB가 not null 이면 임시값
            user.setLastName("User");

            user.setSocialType("KAKAO");
            user.setSocialId(kakaoId);
            user.setVerified(true); // 카카오 로그인 시 이메일 인증 대신 verified=true 처리
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
            userRepository.save(user);
        } else {
            // (B) 기존 유저
            user = optionalUser.get();
            // 필요시 이메일 변경/닉네임 업데이트 등 처리
        }

        // 4) JWT 발급
        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        return new AuthRespDTO(accessToken, refreshToken);
    }

    /**
     * (실제 구현) 카카오에 토큰 요청
     */
    private KakaoTokenResponse getKakaoToken(String code) {
        // 1) 요청 URL
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 2) 파라미터 구성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoRestApiKey);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        // 3) RestTemplate로 POST 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl,
                new HttpEntity<>(params, headers),
                Map.class
        );

        // 4) 응답 파싱
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            String accessToken = (String) body.get("access_token");
            String refreshToken = (String) body.get("refresh_token");
            Integer expiresIn = (Integer) body.get("expires_in");

            return new KakaoTokenResponse(
                    accessToken,
                    refreshToken,
                    (expiresIn != null) ? expiresIn : 0
            );
        } else {
            throw new RuntimeException("카카오 토큰 발급 실패: " + response.getStatusCode());
        }
    }

    /**
     * (실제 구현) 카카오 사용자 정보 가져오기
     */
    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        // 1) 요청 URL
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // 2) 헤더에 Authorization 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // 3) GET 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        // 4) 응답 파싱
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            Object idObj = body.get("id");
            String kakaoId = String.valueOf(idObj); // 숫자라도 문자열로 변환

            // 이메일은 kakao_account 내부에 있을 수 있음
            Map account = (Map) body.get("kakao_account");
            String email = null;
            if (account != null) {
                email = (String) account.get("email");
            }

            return new KakaoUserInfo(kakaoId, email);
        } else {
            throw new RuntimeException("카카오 사용자 정보 조회 실패: " + response.getStatusCode());
        }
    }
}
