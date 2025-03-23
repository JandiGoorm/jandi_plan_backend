package com.jandi.plan_backend.socialLogin.service;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.socialLogin.dto.NaverUserInfo;
import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 네이버 OAuth2 로그인 로직:
 *  1) 인증 URL 생성 (createLoginUrl)
 *  2) 인가코드 & state -> 액세스 토큰
 *  3) 액세스 토큰 -> 사용자 정보
 *  4) DB 조회/가입
 *  5) JWT 발급
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NaverService {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private final UserService userService; // 닉네임 중복 검증 등에 활용
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 1) 로그인 URL 생성 (CSRF 방지용 state 추가)
     */
    public String createLoginUrl(String state) {
        return "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;
    }

    /**
     * 2) 인가코드 & state -> 액세스 토큰
     */
    public String getAccessToken(String code, String state) {
        try {
            String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&state=" + state
                    + "&code=" + code;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(tokenUrl, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("네이버 토큰 요청 실패: " + response.getStatusCode());
            }

            Map body = response.getBody();
            Object accessToken = body.get("access_token");
            if (accessToken == null) {
                throw new RuntimeException("네이버 토큰 요청 실패: access_token이 없음");
            }
            return accessToken.toString();

        } catch (Exception e) {
            throw new RuntimeException("네이버 접근 토큰 요청 중 예외 발생: " + e.getMessage());
        }
    }

    /**
     * 3) 액세스 토큰 -> 네이버 사용자 정보
     */
    public NaverUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("네이버 사용자 정보 요청 실패: " + response.getStatusCode());
            }
            Map body = response.getBody();

            // "response" 필드 안에 실제 유저정보 존재
            Map userResponse = (Map) body.get("response");
            if (userResponse == null) {
                throw new RuntimeException("네이버 사용자 정보 없음: response=null");
            }

            String id = (String) userResponse.get("id");
            String email = (String) userResponse.get("email");

            return new NaverUserInfo(id, email);

        } catch (Exception e) {
            throw new RuntimeException("네이버 사용자 정보 요청 중 예외 발생: " + e.getMessage());
        }
    }

    /**
     * 4) 네이버 사용자 정보 -> DB 조회 or 가입 -> JWT 발급
     *    (컨트롤러에서 code/state로 accessToken, userInfo를 구한 뒤 이 메서드를 호출해도 됨)
     */
    public AuthRespDTO naverLogin(NaverUserInfo userInfo) {
        // (A) 이메일이 null이면 "naver_{id}@naver" 로 임시 구성
        String naverId = userInfo.getId();
        String email = (userInfo.getEmail() != null)
                ? userInfo.getEmail()
                : ("naver_" + naverId + "@naver");

        // (B) DB 조회/가입
        User user = findOrCreateUserByNaver(naverId, email);

        // (C) JWT 발급
        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        return new AuthRespDTO(accessToken, refreshToken);
    }

    /* =================================================================
       (D) DB 조회/가입 로직
     ================================================================= */
    private User findOrCreateUserByNaver(String naverId, String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            // 이미 해당 이메일이 존재
            User user = optionalUser.get();

            // (1) 아직 소셜 연동이 안 된 일반가입 계정
            if (user.getSocialType() == null) {
                user.setSocialType("NAVER");
                user.setSocialId(naverId);
                userRepository.save(user);
            }
            // (2) 이미 다른 소셜로 가입된 경우
            else if (!"NAVER".equals(user.getSocialType())) {
                throw new RuntimeException("이미 다른 소셜("
                        + user.getSocialType() + ")로 가입된 이메일입니다.");
            }
            // (3) 같은 NAVER 타입이지만 ID 다르면 -> 에러
            else if (!Objects.equals(user.getSocialId(), naverId)) {
                throw new RuntimeException("이미 NAVER로 가입된 이메일이지만, ID가 달라 충돌합니다.");
            }
            // (4) 같은 NAVER, 같은 ID -> 그대로 사용
            return user;
        }

        // 이메일 전혀 없으면 새 가입
        String userName = generateNaverUserName(); // 예: NaverUser_0123456789
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setFirstName("Naver");
        newUser.setLastName("User");
        newUser.setEmail(email);
        newUser.setSocialType("NAVER");
        newUser.setSocialId(naverId);
        newUser.setVerified(true);
        newUser.setReported(false);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        newUser.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        userRepository.save(newUser);

        return newUser;
    }

    /* =================================================================
       (E) 네이버 전용 userName 생성 로직 (ex: NaverUser_XXXXXXXXXX)
     ================================================================= */
    private String generateNaverUserName() {
        while (true) {
            long randomNum = 1_000_000_000L + (long) (Math.random() * 9_000_000_000L);
            String userName = "NaverUser_" + randomNum;
            // userService.isExistUserName(...) 활용
            if (!userService.isExistUserName(userName)) {
                return userName;
            }
        }
    }

    /**
     * (선택) 네이버 접근 토큰 삭제 로직
     */
    public String deleteAccessToken(String accessToken) {
        try {
            String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=delete"
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&access_token=" + accessToken
                    + "&service_provider=NAVER";

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(tokenUrl, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("네이버 토큰 삭제 요청 실패: " + response.getStatusCode());
            }
            if (!"success".equals(response.getBody().get("result"))) {
                throw new RuntimeException("네이버 토큰 삭제 요청 실패: result != success");
            }
            return "success";

        } catch (Exception e) {
            throw new RuntimeException("네이버 접근 토큰 삭제 중 예외 발생: " + e.getMessage());
        }
    }
}
