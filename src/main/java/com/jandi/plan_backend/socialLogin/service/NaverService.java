package com.jandi.plan_backend.socialLogin.service;

import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.socialLogin.dto.NaverUserInfo;
import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NaverService {
    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthRespDTO naverLogin(NaverUserInfo userInfo) {
        // 이미 등록된 일반가입 유저인지 확인
        Optional<User> optionalGeneralUser = userRepository.findByEmail(userInfo.getEmail());
        if (optionalGeneralUser.isPresent()) {
            throw new RuntimeException("이미 일반 가입된 이메일입니다. 일반 회원으로 로그인해주세요");
        }

        // 이미 등록된 소셜 유저인지 확인 -> 기존회원이라면 바로 가져오고, 아니라면 회원가입 후 정보가져오기
        Optional<User> optionalNaverUser = userRepository.findBySocialTypeAndSocialId("NAVER", userInfo.getId());
        User user = optionalNaverUser.orElseGet(() -> naverRegister(userInfo));

        // 로그인 처리될 수 있게 토큰 발급
        // 네이버에서 준 접근 토큰 대신 자체 토큰 이용
        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        return new AuthRespDTO(accessToken, refreshToken);
    }

    private User naverRegister(NaverUserInfo userInfo) {
        String naverId = userInfo.getEmail();
        String userEmail = (userInfo.getEmail() != null) ?
                userInfo.getEmail() : "naver_" + naverId + "@naver";

        // 유저 기본 정보(이메일, 이름)
        User user = new User();
        user.setUserName("NaverUser_" + naverId); // 임시 닉네임 부여
        user.setFirstName("Naver");
        user.setLastName("User");
        user.setEmail(userEmail);

        // 소셜 로그인 정보 처리(타입, socialId)
        user.setSocialType("NAVER");
        user.setSocialId(naverId);
        user.setVerified(true); // 네이버 로그인 시 이메일 인증 대신 verified=true 처리
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        // 기타 유저 시간 정보 처리
        user.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        userRepository.save(user);
        return user;
    }

    // 네이버 로그인 인증 요청문 생성
    public String createLoginUrl(String state) {
        return "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;
    }

    // 접근 토큰 요청
    public String getAccessToken(String code, String state) {
        try{
            //요청 url 구성
            String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&state=" + state
                    + "&code=" + code;

            // restTemplate로 GET 요청
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(tokenUrl, Map.class);

            // 반환된 응답: access_token, token_type, expires_in
            Map<String, Object> body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null)
                throw new RuntimeException("네이버 토큰 요청 실패: " + response.getStatusCode());

            Object accessToken = body.get("access_token");
            if(accessToken == null)
                throw new RuntimeException("네이버 토큰 요청 실패: access_token이 존재하지 않습니다.");

            // body 중 필요한 정보인 access_token만 뽑아 리턴
            return accessToken.toString();
        }catch (Exception e){
            throw new RuntimeException("네이버 접근 토큰 요청 중 예외 발생: " + e.getMessage());
        }
    }

    // 네이버 사용자 프로필 정보 조회
    public NaverUserInfo getUserInfo(String accessToken) {
        try{
            // 헤더에 Authorization 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            // restTemplate로 GET 요청
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            // 반환된 응답: resultcode, message, response
            Map<String, Object> body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null)
                throw new RuntimeException("사용자 정보 요청 실패: " + response.getStatusCode());

            // response에 유저 정보 저장됨: email, nickname, profile_image, age, gender, id, name, birthday
            Map<String, Object> userResponse = (Map<String, Object>) body.get("response");
            if(userResponse == null)
                throw new RuntimeException("사용자 정보 요청 실패: response가 존재하지 않습니다");

            // response의 정보를 DTO로 감싸서 리턴
            NaverUserInfo naverUserInfo = new NaverUserInfo();
            naverUserInfo.setId(userResponse.get("id").toString());
            naverUserInfo.setEmail(userResponse.get("email").toString());
            return naverUserInfo;
        }catch (Exception e){
            throw new RuntimeException("사용자 정보 요청 중 예외 발생: " + e.getMessage());
        }
    }

    // 접근 토큰 삭제
    public String deleteAccessToken(String accessToken) {
        try{
            //요청 url 구성
            String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=delete"
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&access_token=" + accessToken
                    + "&service_provider=" + "NAVER";

            // restTemplate로 GET 요청
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(tokenUrl, Map.class);

            // 반환된 응답: access_token, result
            Map<String, Object> body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null)
                throw new RuntimeException("네이버 토큰 삭제 요청 실패: " + response.getStatusCode());
            if(!body.get("result").equals("success"))
                throw new RuntimeException("네이버 토큰 삭제 요청 실패: result != success");

            // 성공 여부 반환
            return (String) body.get("result");
        }catch (Exception e){
            throw new RuntimeException("접근 토큰 삭제 중 예외 발생: " + e.getMessage());
        }
    }
}

