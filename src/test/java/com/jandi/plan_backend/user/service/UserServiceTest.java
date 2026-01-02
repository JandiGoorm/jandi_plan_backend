package com.jandi.plan_backend.user.service;

import com.jandi.plan_backend.fixture.UserFixture;
import com.jandi.plan_backend.security.JwtTokenProvider;
import com.jandi.plan_backend.user.dto.AuthRespDTO;
import com.jandi.plan_backend.user.dto.ChangePasswordDTO;
import com.jandi.plan_backend.user.dto.UserLoginDTO;
import com.jandi.plan_backend.user.dto.UserRegisterDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.user.repository.UserRepository;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 블랙박스 단위 테스트
 * 
 * 테스트 대상: 회원가입, 로그인, 이메일 인증, 비밀번호 변경, 토큰 갱신
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private UserService userService;

    private User normalUser;
    private User unverifiedUser;

    @BeforeEach
    void setUp() {
        normalUser = UserFixture.createNormalUser();
        unverifiedUser = UserFixture.createUnverifiedUser();
    }

    // ==================== 회원가입 테스트 ====================

    @Nested
    @DisplayName("회원가입")
    class RegisterTest {

        @Test
        @DisplayName("[성공] 유효한 정보로 회원가입 시 사용자 생성")
        void registerUser_WithValidInput_ShouldCreateUser() {
            // given
            UserRegisterDTO dto = UserFixture.createValidRegisterDTO();
            when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setUserId(1);
                return savedUser;
            });

            // when
            User result = userService.registerUser(dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(dto.getEmail());
            assertThat(result.getUserName()).isEqualTo(dto.getUserName());
            assertThat(result.getVerified()).isFalse();
            assertThat(result.getVerificationToken()).isNotNull();

            verify(userRepository).save(any(User.class));
            verify(emailService).sendSimpleMail(eq(dto.getEmail()), anyString(), anyString());
        }

        @Test
        @DisplayName("[실패] 중복 이메일로 회원가입 시 예외 발생")
        void registerUser_WithDuplicateEmail_ShouldThrowException() {
            // given
            UserRegisterDTO dto = UserFixture.createValidRegisterDTO();
            when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.registerUser(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("이미 존재하는 이메일");

            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendSimpleMail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("[성공] 회원가입 시 인증 토큰이 생성되고 만료 시간이 설정됨")
        void registerUser_ShouldCreateVerificationTokenWithExpiry() {
            // given
            UserRegisterDTO dto = UserFixture.createValidRegisterDTO();
            when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setUserId(1);
                return savedUser;
            });

            // when
            User result = userService.registerUser(dto);

            // then
            assertThat(result.getVerificationToken()).isNotNull();
            assertThat(result.getVerificationToken()).isNotEmpty();
            assertThat(result.getTokenExpires()).isNotNull();
            assertThat(result.getTokenExpires()).isAfter(java.time.LocalDateTime.now());
        }
    }

    // ==================== 로그인 테스트 ====================

    @Nested
    @DisplayName("로그인")
    class LoginTest {

        @Test
        @DisplayName("[성공] 유효한 자격증명으로 로그인 시 JWT 토큰 발급")
        void login_WithValidCredentials_ShouldReturnTokens() {
            // given
            UserLoginDTO dto = UserFixture.createValidLoginDTO();
            when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(normalUser));
            when(passwordEncoder.matches(dto.getPassword(), normalUser.getPassword())).thenReturn(true);
            when(jwtTokenProvider.createToken(dto.getEmail())).thenReturn("accessToken");
            when(jwtTokenProvider.createRefreshToken(dto.getEmail())).thenReturn("refreshToken");

            // when
            AuthRespDTO result = userService.login(dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("accessToken");
            assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 이메일로 로그인 시 예외 발생")
        void login_WithNonExistentEmail_ShouldThrowException() {
            // given
            UserLoginDTO dto = UserFixture.createValidLoginDTO();
            when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("존재하지 않는 이메일");
        }

        @Test
        @DisplayName("[실패] 잘못된 비밀번호로 로그인 시 예외 발생")
        void login_WithWrongPassword_ShouldThrowException() {
            // given
            UserLoginDTO dto = UserFixture.createValidLoginDTO();
            when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(normalUser));
            when(passwordEncoder.matches(dto.getPassword(), normalUser.getPassword())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("[실패] 이메일 미인증 사용자 로그인 시 예외 발생")
        void login_WithUnverifiedUser_ShouldThrowException() {
            // given
            UserLoginDTO dto = new UserLoginDTO();
            dto.setEmail(unverifiedUser.getEmail());
            dto.setPassword("password123!");

            when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(unverifiedUser));
            when(passwordEncoder.matches(dto.getPassword(), unverifiedUser.getPassword())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.login(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("이메일 인증이 필요합니다");
        }
    }

    // ==================== 이메일 인증 테스트 ====================

    @Nested
    @DisplayName("이메일 인증")
    class VerifyEmailTest {

        @Test
        @DisplayName("[성공] 유효한 토큰으로 이메일 인증 성공")
        void verifyEmail_WithValidToken_ShouldReturnTrue() {
            // given
            String token = "valid-token";
            when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(unverifiedUser));
            unverifiedUser.setTokenExpires(LocalDateTime.now().plusHours(1));

            // when
            boolean result = userService.verifyEmailByToken(token);

            // then
            assertThat(result).isTrue();
            assertThat(unverifiedUser.getVerified()).isTrue();
            assertThat(unverifiedUser.getVerificationToken()).isNull();
            verify(userRepository).save(unverifiedUser);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 토큰으로 인증 시 false 반환")
        void verifyEmail_WithInvalidToken_ShouldReturnFalse() {
            // given
            String token = "invalid-token";
            when(userRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

            // when
            boolean result = userService.verifyEmailByToken(token);

            // then
            assertThat(result).isFalse();
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("[실패] 만료된 토큰으로 인증 시 false 반환")
        void verifyEmail_WithExpiredToken_ShouldReturnFalse() {
            // given
            String token = "expired-token";
            User expiredUser = UserFixture.createUnverifiedUser();
            expiredUser.setTokenExpires(LocalDateTime.now().minusHours(1));
            when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(expiredUser));

            // when
            boolean result = userService.verifyEmailByToken(token);

            // then
            assertThat(result).isFalse();
            verify(userRepository, never()).save(any(User.class));
        }
    }

    // ==================== 토큰 갱신 테스트 ====================

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshTokenTest {

        @Test
        @DisplayName("[성공] 유효한 리프레시 토큰으로 새 토큰 발급")
        void refreshToken_WithValidToken_ShouldReturnNewTokens() {
            // given
            String refreshToken = "validRefreshToken";
            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(jwtTokenProvider.getEmail(refreshToken)).thenReturn(normalUser.getEmail());
            when(jwtTokenProvider.createToken(normalUser.getEmail())).thenReturn("newAccessToken");
            when(jwtTokenProvider.createRefreshToken(normalUser.getEmail())).thenReturn("newRefreshToken");

            // when
            AuthRespDTO result = userService.refreshToken(refreshToken);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("newAccessToken");
            assertThat(result.getRefreshToken()).isEqualTo("newRefreshToken");
        }

        @Test
        @DisplayName("[실패] 유효하지 않은 리프레시 토큰으로 갱신 시 예외 발생")
        void refreshToken_WithInvalidToken_ShouldThrowException() {
            // given
            String invalidToken = "invalidToken";
            when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.refreshToken(invalidToken))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("리프레시 토큰이 유효하지 않습니다");
        }
    }

    // ==================== 비밀번호 변경 테스트 ====================

    @Nested
    @DisplayName("비밀번호 변경")
    class ChangePasswordTest {

        @Test
        @DisplayName("[성공] 올바른 현재 비밀번호로 비밀번호 변경")
        void changePassword_WithCorrectCurrentPassword_ShouldSucceed() {
            // given
            String email = normalUser.getEmail();
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setCurrentPassword("currentPassword");
            dto.setNewPassword("newPassword123!");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(normalUser));
            when(passwordEncoder.matches(dto.getCurrentPassword(), normalUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(dto.getNewPassword())).thenReturn("encodedNewPassword");

            // when
            userService.changePassword(email, dto);

            // then
            verify(userRepository).save(normalUser);
            assertThat(normalUser.getPassword()).isEqualTo("encodedNewPassword");
        }

        @Test
        @DisplayName("[실패] 잘못된 현재 비밀번호로 변경 시 예외 발생")
        void changePassword_WithWrongCurrentPassword_ShouldThrowException() {
            // given
            String email = normalUser.getEmail();
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setCurrentPassword("wrongPassword");
            dto.setNewPassword("newPassword123!");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(normalUser));
            when(passwordEncoder.matches(dto.getCurrentPassword(), normalUser.getPassword())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.changePassword(email, dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("현재 비밀번호가 올바르지 않습니다");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    // ==================== 이메일/닉네임 중복 확인 테스트 ====================

    @Nested
    @DisplayName("중복 확인")
    class DuplicateCheckTest {

        @Test
        @DisplayName("[성공] 존재하는 이메일 확인 시 true 반환")
        void isExistEmail_WithExistingEmail_ShouldReturnTrue() {
            // given
            when(userRepository.findByEmail(normalUser.getEmail())).thenReturn(Optional.of(normalUser));

            // when
            boolean result = userService.isExistEmail(normalUser.getEmail());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 이메일 확인 시 false 반환")
        void isExistEmail_WithNonExistingEmail_ShouldReturnFalse() {
            // given
            when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

            // when
            boolean result = userService.isExistEmail("new@example.com");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[성공] 존재하는 닉네임 확인 시 true 반환")
        void isExistUserName_WithExistingUserName_ShouldReturnTrue() {
            // given
            when(userRepository.findByUserName(normalUser.getUserName())).thenReturn(Optional.of(normalUser));

            // when
            boolean result = userService.isExistUserName(normalUser.getUserName());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 닉네임 확인 시 false 반환")
        void isExistUserName_WithNonExistingUserName_ShouldReturnFalse() {
            // given
            when(userRepository.findByUserName("newUserName")).thenReturn(Optional.empty());

            // when
            boolean result = userService.isExistUserName("newUserName");

            // then
            assertThat(result).isFalse();
        }
    }

    // ==================== 닉네임 변경 테스트 ====================

    @Nested
    @DisplayName("닉네임 변경")
    class ChangeUserNameTest {

        @Test
        @DisplayName("[성공] 중복되지 않은 닉네임으로 변경")
        void changeUserName_WithUniqueUserName_ShouldSucceed() {
            // given
            String email = normalUser.getEmail();
            String newUserName = "newUniqueUserName";

            when(validationUtil.validateUserExists(email)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(userRepository.findByUserName(newUserName)).thenReturn(Optional.empty());

            // when
            userService.changeUserName(email, newUserName);

            // then
            verify(userRepository).save(normalUser);
            assertThat(normalUser.getUserName()).isEqualTo(newUserName);
        }

        @Test
        @DisplayName("[실패] 빈 닉네임으로 변경 시 예외 발생")
        void changeUserName_WithEmptyUserName_ShouldThrowException() {
            // given
            String email = normalUser.getEmail();
            String newUserName = "";

            when(validationUtil.validateUserExists(email)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);

            // when & then
            assertThatThrownBy(() -> userService.changeUserName(email, newUserName))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("새 닉네임이 비어있습니다");
        }

        @Test
        @DisplayName("[실패] 중복된 닉네임으로 변경 시 예외 발생")
        void changeUserName_WithDuplicateUserName_ShouldThrowException() {
            // given
            String email = normalUser.getEmail();
            String duplicateUserName = "existingUser";
            User existingUser = UserFixture.createUserWithIdAndEmail(99, "other@example.com");
            existingUser.setUserName(duplicateUserName);

            when(validationUtil.validateUserExists(email)).thenReturn(normalUser);
            doNothing().when(validationUtil).validateUserRestricted(normalUser);
            when(userRepository.findByUserName(duplicateUserName)).thenReturn(Optional.of(existingUser));

            // when & then
            assertThatThrownBy(() -> userService.changeUserName(email, duplicateUserName))
                    .isInstanceOf(BadRequestExceptionMessage.class)
                    .hasMessageContaining("이미 사용중인 닉네임입니다");
        }
    }

    // ==================== 비밀번호 찾기 테스트 ====================

    @Nested
    @DisplayName("비밀번호 찾기")
    class ForgotPasswordTest {

        @Test
        @DisplayName("[성공] 존재하는 이메일로 임시 비밀번호 발송")
        void forgotPassword_WithExistingEmail_ShouldSendTempPassword() {
            // given
            String email = normalUser.getEmail();
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(normalUser));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");

            // when
            userService.forgotPassword(email);

            // then
            verify(userRepository).save(normalUser);
            verify(emailService).sendSimpleMail(eq(email), contains("비밀번호"), anyString());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 이메일로 비밀번호 찾기 시 예외 발생")
        void forgotPassword_WithNonExistingEmail_ShouldThrowException() {
            // given
            String email = "nonexistent@example.com";
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.forgotPassword(email))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("해당 이메일을 사용하는 사용자가 없습니다");

            verify(emailService, never()).sendSimpleMail(anyString(), anyString(), anyString());
        }
    }
}
