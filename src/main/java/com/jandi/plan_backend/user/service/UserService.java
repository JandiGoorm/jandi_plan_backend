package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.commu.dto.UserListDTO;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.service.ImageService;
import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.user.dto.ChangePasswordDTO;
import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.dto.UserInfoRespDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import com.jandi.plan_backend.util.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

/**
 * 사용자 관련 비즈니스 로직을 담당하는 서비스 클래스.
 * 회원가입, 로그인, 이메일 인증, 비밀번호 재발급, 사용자 상세 정보 조회,
 * 비밀번호 변경, 리프레시 토큰 갱신, 회원 탈퇴(계정 삭제) 등의 기능을 제공한다.
 */
@Slf4j
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ImageService imageService;
    private final ValidationUtil validationUtil;

    @Value("${app.verify.url}")
    private String verifyUrl;

    public UserService(UserRepository userRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       ImageService imageService, ValidationUtil validationUtil) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.imageService = imageService;
        this.validationUtil = validationUtil;
    }

    public User registerUser(UserRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        user.setVerified(false);
        user.setReported(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpires(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusHours(24));
        userRepository.save(user);
        String verifyLink = verifyUrl + "?token=" + token;
        String subject = "[회원가입] 이메일 인증 안내";
        String text = "안녕하세요.\n"
                + "아래 링크를 클릭하면 이메일 인증이 완료됩니다.\n\n"
                + verifyLink
                + "\n\n인증은 24시간 이내에 완료해주세요.";
        emailService.sendSimpleMail(user.getEmail(), subject, text);
        return user;
    }

    /**
     * 로그인 기능을 수행한다.
     * 이메일과 비밀번호를 검증하고, 인증에 성공하면 액세스 토큰과 리프레시 토큰을 발급한다.
     *
     * @param dto 로그인 정보를 담은 DTO
     * @return AuthRespDTO 객체 (액세스 토큰, 리프레시 토큰)
     */
    public AuthRespDTO login(UserLoginDTO dto) {
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("존재하지 않는 이메일입니다.");
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        if (!user.getVerified()) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }
        String accessToken = jwtTokenProvider.createToken(dto.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(dto.getEmail());
        return new AuthRespDTO(accessToken, refreshToken);
    }

    /**
     * 리프레시 토큰을 이용해 새 액세스 토큰(및 리프레시 토큰)을 발급한다.
     *
     * @param refreshToken 클라이언트가 전송한 리프레시 토큰
     * @return 새로 발급된 토큰들을 포함한 AuthRespDTO 객체
     */
    public AuthRespDTO refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 유효하지 않습니다.");
        }
        String email = jwtTokenProvider.getEmail(refreshToken);
        String newAccessToken = jwtTokenProvider.createToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);
        return new AuthRespDTO(newAccessToken, newRefreshToken);
    }

    public boolean verifyEmailByToken(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isEmpty()) {
            return false;
        }
        User user = optionalUser.get();
        if (user.getTokenExpires() != null && user.getTokenExpires().isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
            return false;
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpires(null);
        user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        userRepository.save(user);
        return true;
    }

    public void forgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("해당 이메일을 사용하는 사용자가 없습니다.");
        }
        User user = optionalUser.get();
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        userRepository.save(user);
        String subject = "[비밀번호 찾기] 임시 비밀번호 안내";
        String text = "임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경하세요.";
        emailService.sendSimpleMail(email, subject, text);
    }

    public UserInfoRespDTO getUserInfo(Integer userId) {
        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // 2. 기본 DTO 생성
        UserInfoRespDTO dto = new UserInfoRespDTO();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setUsername(user.getUserName());
        dto.setVerified(user.getVerified());
        dto.setReported(user.getReported());

        // 3. 사용자 프로필 이미지 조회
        //    targetType이 "userProfile"이고, targetId가 userId인 Image를 조회
        Optional<Image> optionalProfileImage = imageService.getImageByTarget("userProfile", user.getUserId());

        String profileImageUrl;
        if (optionalProfileImage.isPresent()) {
            // (1) 프로필 이미지가 존재하면 해당 이미지 URL 사용
            Image profileImage = optionalProfileImage.get();
            profileImageUrl = "https://storage.googleapis.com/plan-storage/" + profileImage.getImageUrl();
        } else {
            // (2) 프로필 이미지가 없으면 imageId=1(가정) 인 이미지를 대신 사용
            String fallbackUrl = imageService.getPublicUrlByImageId(1);  // imageId=1로 조회
            // fallbackUrl이 null인 경우(1번 이미지도 없다면) 최종 null 처리
            profileImageUrl = (fallbackUrl != null) ? fallbackUrl : null;
        }

        dto.setProfileImageUrl(profileImageUrl);

        return dto;
    }

    public void changePassword(String email, ChangePasswordDTO dto) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
        userRepository.save(user);
    }

    /**
     * 회원 탈퇴(계정 삭제) 기능을 수행한다.
     * 인증된 사용자의 이메일을 기반으로 사용자 정보를 조회한 후,
     * 해당 사용자와 연결된 프로필 이미지(타겟 타입 "userProfile")를 먼저 삭제하고,
     * 이후 사용자 계정을 삭제한다.
     *
     * @param email 인증된 사용자의 이메일
     * @throws RuntimeException 사용자를 찾을 수 없는 경우 예외 발생
     */
    public void deleteUser(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        User user = optionalUser.get();

        // 프로필 이미지 삭제: targetType이 'userProfile'이고, targetId가 사용자의 userId인 이미지를 삭제
        imageService.getImageByTarget("userProfile", user.getUserId())
                .ifPresent(img -> imageService.deleteImage(img.getImageId()));

        // 이후 사용자 삭제
        userRepository.delete(user);
    }

    // 중복 이메일 검증
    public boolean isExistEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // 중복 닉네임 검증
    public boolean isExistUserName(String userName) {
        return userRepository.findByUserName(userName).isPresent();
    }

    /** 관리자 전용 */
    //유저 목록 로드
    public Page<UserListDTO> getAllUsers(String userEmail, int page, int size) {
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        long totalCount = userRepository.count();
        return PaginationService.getPagedData(page, size, totalCount,
                userRepository::findAll, UserListDTO::new);
    }

    //유저 제재하기
    public Boolean permitUser(String userEmail, Integer userId) {
        User admin = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserIsAdmin(admin);

        User user = userRepository.findByUserId(userId).orElse(null);
        if(user==null) { //사용자 미존재 시 오류 반환
            throw new BadRequestExceptionMessage("사용자가 존재하지 않습니다");
        }
        log.info("선택된 유저: {}{}", user.getEmail(), user.getReported() ? "(제재)" : "(일반)");

        //유저 제재/제한 해제 후 제한 여부 반환
        user.setReported(!user.getReported());
        userRepository.save(user);
        return user.getReported();
    }
}
