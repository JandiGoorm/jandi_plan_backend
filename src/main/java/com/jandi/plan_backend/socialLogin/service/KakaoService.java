package com.jandi.plan_backend.socialLogin.service;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.socialLogin.dto.KakaoTokenResponse;
import com.jandi.plan_backend.socialLogin.dto.KakaoUserInfo;
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

import com.jandi.plan_backend.util.TimeUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 카카오 OAuth2 로그인 로직:
 *  1) 인가코드(code) -> 카카오 토큰 요청
 *  2) 카카오 토큰 -> 사용자 정보 조회
 *  3) DB 조회/가입
 *  4) JWT 발급
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
     * 카카오 로그인 절차 메인 메서드
     *  (1) 인가코드 -> 토큰
     *  (2) 토큰 -> 사용자 정보
     *  (3) DB 조회 or 가입
     *  (4) JWT 발급
     */
    public AuthRespDTO kakaoLogin(String code) {
        // 1) 카카오 토큰 요청
        KakaoTokenResponse tokenResponse = requestKakaoToken(code);

        // 2) 카카오 사용자 정보 요청
        KakaoUserInfo userInfo = requestKakaoUserInfo(tokenResponse.getAccessToken());
        String kakaoId = userInfo.getId();

        // 이메일이 없으면 "kakao_{kakaoId}@kakao"로 임시 구성
        String email = (userInfo.getEmail() != null)
                ? userInfo.getEmail()
                : ("kakao_" + kakaoId + "@kakao");

        // 3) DB에서 해당 이메일로 User 찾기 or 신규 생성
        User user = findOrCreateUserByKakao(kakaoId, email);

        // 4) JWT 발급
        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        return new AuthRespDTO(accessToken, refreshToken);
    }

    /* =================================================================
       (A) 카카오 토큰 요청
     ================================================================= */
    private KakaoTokenResponse requestKakaoToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 요청 파라미터
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoRestApiKey);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        // 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl,
                new HttpEntity<>(params, headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("카카오 토큰 발급 실패: " + response.getStatusCode());
        }

        Map body = response.getBody();
        String accessToken = (String) body.get("access_token");
        String refreshToken = (String) body.get("refresh_token");
        Integer expiresIn = (Integer) body.get("expires_in");

        return new KakaoTokenResponse(
                accessToken,
                refreshToken,
                (expiresIn != null) ? expiresIn : 0
        );
    }

    /* =================================================================
       (B) 카카오 사용자 정보 조회
     ================================================================= */
    private KakaoUserInfo requestKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // 헤더에 Authorization 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // GET 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패: " + response.getStatusCode());
        }

        Map body = response.getBody();
        // 카카오에서 넘어오는 id (숫자 -> String)
        String kakaoId = String.valueOf(body.get("id"));

        // 이메일은 kakao_account 내부
        Map account = (Map) body.get("kakao_account");
        String email = null;
        if (account != null) {
            email = (String) account.get("email");
        }

        return new KakaoUserInfo(kakaoId, email);
    }

    /* =================================================================
       (C) DB 조회/가입 로직
     ================================================================= */
    private User findOrCreateUserByKakao(String kakaoId, String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            // 이미 이메일이 존재
            User user = optionalUser.get();

            // (1) 아직 소셜 연동이 안 된 일반가입 계정 -> 소셜 정보 등록
            if (user.getSocialType() == null) {
                user.setSocialType("KAKAO");
                user.setSocialId(kakaoId);
                userRepository.save(user);
            }
            // (2) 이미 다른 소셜로 가입된 경우 -> 에러
            else if (!"KAKAO".equals(user.getSocialType())) {
                throw new RuntimeException("이미 다른 소셜("
                        + user.getSocialType() + ")로 가입된 이메일입니다.");
            }
            // (3) 같은 KAKAO 타입이지만, ID가 다르면 -> 에러
            else if (!Objects.equals(user.getSocialId(), kakaoId)) {
                throw new RuntimeException("이미 KAKAO로 가입된 이메일이지만, ID가 달라 충돌합니다.");
            }
            // (4) 같은 KAKAO, 같은 ID -> 그대로 사용
            return user;
        }

        // 이메일 자체가 전혀 없으면 새로 가입
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUserName("kakaoUser_" + kakaoId);
        newUser.setFirstName("Kakao");
        newUser.setLastName("User");
        newUser.setSocialType("KAKAO");
        newUser.setSocialId(kakaoId);
        newUser.setVerified(true);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setCreatedAt(TimeUtil.now());
        newUser.setUpdatedAt(TimeUtil.now());

        userRepository.save(newUser);
        return newUser;
    }
}
