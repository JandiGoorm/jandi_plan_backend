# 테스트 코드 작성 계획

## 개요

본 프로젝트는 **블랙박스 단위 테스트(Black-box Unit Testing)** 전략을 채택한다. Service 레이어를 중심으로 입력과 출력만으로 기능을 검증하며, 내부 구현에는 의존하지 않는다.

> **마지막 업데이트**: 2026-01-02
> **전체 테스트 수**: 166개 (전체 통과)
> **테스트 커버리지 현황**: Phase 1, Phase 2 완료

---

## 1. 테스트 전략

### 1.1 블랙박스 테스트 vs 화이트박스 테스트

본 프로젝트는 **블랙박스 테스트**를 주 전략으로 채택한다.

| 구분 | 블랙박스 테스트 | 화이트박스 테스트 |
|------|----------------|------------------|
| **관점** | 외부 (사용자/요구사항) | 내부 (코드 구조) |
| **검증 대상** | 입력 → 출력 | 분기, 경로, 조건 |
| **장점** | 기능 검증에 적합, 구현 변경에 강함 | 높은 코드 커버리지 |
| **적용** | 기능 구현 검증 (본 프로젝트 채택) | 복잡한 알고리즘 검증 |

**블랙박스 테스트를 채택한 이유**:
- 기능 구현 검증에 더 적합
- 요구사항 기반으로 사용자 관점 검증
- 내부 구현 변경 시에도 테스트 수정 불필요
- 개발 중에는 동등분할 테스트 위주로 작성

**핵심 질문**: 이 입력을 주면 이 출력이 나오는가?

### 1.2 동등분할 테스트 (Equivalence Partitioning)

개발 중에는 **동등분할 테스트**를 위주로 작성한다.

```
동등분할: 입력 데이터를 동일한 결과를 기대하는 그룹으로 분류

┌──────────────────────────────────────────┐
│           입력 도메인                      │
├─────────────┬────────────┬───────────────┤
│  유효 입력   │  경계값    │   무효 입력    │
│  (정상 케이스)│ (선택적)   │  (예외 케이스) │
└─────────────┴────────────┴───────────────┘

예시 - 좋아요 기능:
• 유효 입력: 타인의 게시물에 좋아요 → 성공
• 무효 입력1: 본인 게시물에 좋아요 → 예외
• 무효 입력2: 이미 좋아요한 게시물 → 예외
• 무효 입력3: 제한된 사용자가 좋아요 → 예외
```

### 1.3 테스트 대상

Service 레이어의 주요 메서드들을 테스트한다.

```
테스트 범위:
- 입력: DTO, 파라미터
- 출력: 반환값, 예외
- 의존성: Repository, 외부 서비스 → Mock 처리
```

### 1.4 테스트 도구

| 도구 | 용도 |
|------|------|
| JUnit 5 | 테스트 프레임워크 |
| Mockito | Mock 객체 생성 |
| AssertJ | 가독성 좋은 assertion |
| @ExtendWith(MockitoExtension.class) | Mock 자동 주입 |

### 1.5 테스트 명명 규칙

메서드명은 다음 형식을 따른다:

```
테스트명_입력조건_기대출력
```

예시:
```java
@Test
@DisplayName("[성공] 정상 입력으로 회원가입 시 사용자 생성")
void registerUser_WithValidInput_ShouldCreateUser() { ... }

@Test
@DisplayName("[실패] 중복 이메일로 회원가입 시 예외 발생")
void registerUser_WithDuplicateEmail_ShouldThrowException() { ... }
```

---

## 2. 도메인별 테스트 설계

### 2.1 User Domain

#### UserService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 회원가입 성공 | 유효한 UserRegisterDTO | User 생성, 인증 메일 발송 | 🔴 필수 |
| 중복 이메일 거절 | 기존 이메일 | BadRequestException | 🔴 필수 |
| 중복 닉네임 거절 | 기존 닉네임 | BadRequestException | 🔴 필수 |
| 로그인 성공 | 유효한 email, password | AuthRespDTO (JWT) | 🔴 필수 |
| 로그인 실패 - 잘못된 비밀번호 | 틀린 password | BadRequestException | 🔴 필수 |
| 인증 안 된 사용자 로그인 거절 | 미인증 email | BadRequestException | 🟡 권장 |
| 이메일 인증 성공 | 유효한 token | verified = true | 🟡 권장 |
| 토큰 갱신 성공 | 유효한 refreshToken | 새 토큰 발급 | 🟡 권장 |
| 비밀번호 변경 | 기존 비밀번호 일치 | 비밀번호 변경 완료 | 🟡 권장 |
| 닉네임 변경 | 중복 아닌 닉네임 | 닉네임 변경 완료 | 🟢 선택 |
| 회원 탈퇴 | 인증된 사용자 | 사용자 삭제 | 🟢 선택 |

#### PreferTripService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 대륙 목록 조회 | filter | List<ContinentRespDTO> | 🟢 선택 |
| 선호 도시 조회 | userEmail | List<CityRespDTO> | 🟡 권장 |
| 선호 도시 등록 | cityIds, userEmail | 등록 개수 반환 | 🟡 권장 |

---

### 2.2 Community Domain

#### CommunityQueryService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 게시물 목록 조회 | page, size | Page<CommunityListDTO> | 🔴 필수 |
| 게시물 상세 조회 (비로그인) | postId, null | CommunityItemDTO | 🔴 필수 |
| 게시물 상세 조회 (로그인) | postId, userEmail | liked 정보 포함 | 🔴 필수 |
| 존재하지 않는 게시물 조회 | 없는 postId | BadRequestException | 🔴 필수 |

#### CommunityUpdateService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 게시물 생성 성공 | PostFinalizeReqDTO | CommunityRespDTO | 🔴 필수 |
| 제한된 사용자 게시물 거절 | restricted user | BadRequestException | 🔴 필수 |
| 게시물 수정 성공 | postId, CommunityReqDTO | CommunityRespDTO | 🔴 필수 |
| 타인 게시물 수정 거절 | 다른 user의 postId | BadRequestException | �� 필수 |
| 게시물 삭제 성공 | postId, userEmail | 삭제된 댓글 수 반환 | 🔴 필수 |
| 타인 게시물 삭제 거절 | 다른 user의 postId | BadRequestException | 🔴 필수 |

#### CommunityLikeService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 좋아요 성공 | userEmail, postId | 좋아요 생성 | 🟡 권장 |
| 중복 좋아요 거절 | 이미 좋아요한 postId | BadRequestException | 🟡 권장 |
| 좋아요 취소 | userEmail, postId | 좋아요 삭제 | 🟡 권장 |

#### CommunityReportService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 게시물 신고 성공 | userEmail, postId, reason | PostReportRespDTO | 🟡 권장 |
| 중복 신고 거절 | 이미 신고한 postId | BadRequestException | 🟡 권장 |
| 본인 게시물 신고 거절 | 본인의 postId | BadRequestException | 🟡 권장 |

#### CommunitySearchService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 제목 검색 | "TITLE", keyword | 제목 일치 게시물 | 🟢 선택 |
| 작성자 검색 | "AUTHOR", keyword | 작성자 일치 게시물 | 🟢 선택 |

---

### 2.3 Comment Domain

#### CommentQueryService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 댓글 목록 조회 | postId, page, size | Page<ParentCommentDTO> | 🔴 필수 |
| 답글 목록 조회 | commentId, page, size | Page<RepliesDTO> | 🟡 권장 |

#### CommentUpdateService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 댓글 작성 성공 | postId, CommentReqDTO | CommentRespDTO | 🔴 필수 |
| 답글 작성 성공 | commentId, CommentReqDTO | CommentRespDTO | 🔴 필수 |
| 댓글 수정 성공 | commentId, CommentReqDTO | CommentRespDTO | 🟡 권장 |
| 댓글 삭제 성공 | commentId | 삭제된 답글 수 반환 | 🟡 권장 |
| 타인 댓글 삭제 거절 | 다른 user의 commentId | BadRequestException | 🟡 권장 |

---

### 2.4 TripPlan Domain

#### TripService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 여행 목록 조회 | page, size | Page<TripRespDTO> | 🔴 필수 |
| 내 여행 조회 | userEmail, page, size | Page<MyTripRespDTO> | 🔴 필수 |
| 여행 상세 조회 | tripId | TripItemRespDTO | 🔴 필수 |
| 비공개 여행 조회 거절 | 타인의 비공개 tripId | BadRequestException | 🔴 필수 |
| 여행 생성 | TripCreateReqDTO | TripRespDTO | 🔴 필수 |
| 여행 수정 | tripId, TripUpdateReqDTO | TripRespDTO | 🟡 권장 |
| 여행 삭제 | tripId, userEmail | 삭제 완료 메시지 | 🟡 권장 |
| 여행 좋아요 | userEmail, tripId | TripLikeRespDTO | 🟡 권장 |

#### ItineraryQueryService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 일정 목록 조회 | tripId, userEmail | List<ItineraryRespDTO> | 🔴 필수 |
| 비공개 여행 일정 조회 거절 | 타인의 비공개 tripId | BadRequestException | 🔴 필수 |

#### ItineraryUpdateService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 일정 생성 | tripId, ItineraryReqDTO | ItineraryRespDTO | 🔴 필수 |
| 일정 수정 | itineraryId, ItineraryReqDTO | ItineraryRespDTO | 🟡 권장 |
| 일정 삭제 | itineraryId | 삭제 완료 | 🟡 권장 |

#### ReservationQueryService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 예약 정보 조회 | tripId, userEmail | Map (reservations) | 🟡 권장 |

#### ReservationUpdateService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 예약 생성 | tripId, ReservationReqDTO | ReservationRespDTO | 🟡 권장 |
| 예약 수정 | reservationId, ReservationReqDTO | ReservationRespDTO | 🟢 선택 |

---

### 2.5 Resource Domain

#### BannerQueryService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 배너 목록 조회 | - | List<BannerListDTO> | 🟡 권장 |

#### BannerUpdateService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 배너 생성 (관리자) | BannerReqDTO | BannerRespDTO | 🟡 권장 |
| 배너 수정 (관리자) | bannerId, BannerReqDTO | BannerRespDTO | 🟢 선택 |
| 배너 삭제 (관리자) | bannerId | 삭제 완료 | 🟢 선택 |

#### NoticeQueryService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 공지 목록 조회 | - | List<NoticeListDTO> | 🟡 권장 |

#### NoticeUpdateService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 공지 생성 (관리자) | NoticeFinalizeReqDTO | NoticeRespDTO | 🟡 권장 |
| 공지 수정 (관리자) | noticeId, NoticeReqDTO | NoticeRespDTO | 🟢 선택 |
| 공지 삭제 (관리자) | noticeId | 삭제 완료 | 🟢 선택 |

---

### 2.6 Admin Domain

#### ManageUserService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 전체 사용자 조회 | page, size | Page<UserListDTO> | 🟡 권장 |
| 제한 사용자 조회 | page, size | Page<UserListDTO> | 🟡 권장 |
| 사용자 제한 토글 | userId | Boolean (제한여부) | 🟡 권장 |
| 관리자 권한 검증 | 일반 사용자 | Exception | 🟡 권장 |

#### ManageCommunityService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 신고 게시물 조회 | page, size | Page<CommunityReportedListDTO> | 🟡 권장 |
| 신고 댓글 조회 | page, size | Page<CommentReportedListDTO> | 🟡 권장 |
| 신고 게시물 삭제 | postId | 삭제 완료 | 🟢 선택 |
| 신고 댓글 삭제 | commentId | 삭제 완료 | 🟢 선택 |

#### ManageTripService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 대륙 생성 (관리자) | continentName, file | ContinentRespDTO | 🟢 선택 |
| 국가 생성 (관리자) | continentName, countryName | CountryRespDTO | 🟢 선택 |
| 도시 생성 (관리자) | countryName, cityName, file, lat, lng | CityRespDTO | 🟢 선택 |
| 국가 수정 (관리자) | countryId, countryName | CountryRespDTO | 🟢 선택 |
| 도시 수정 (관리자) | cityId, cityName, description | CityRespDTO | 🟢 선택 |
| 국가 삭제 (관리자) | countryId | 삭제된 도시 수 반환 | 🟢 선택 |
| 도시 삭제 (관리자) | cityId | 삭제 완료 | 🟢 선택 |
| 일반 사용자 접근 거절 | 일반 사용자 | Exception | 🟢 선택 |

---

### 2.7 Image Domain

#### ImageService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 이미지 업로드 성공 | file, ownerEmail, targetId, targetType | ImageRespDto | 🟡 권장 |
| 빈 파일 업로드 거절 | empty file | Exception | 🟡 권장 |
| 지원하지 않는 형식 거절 | .exe file | Exception | 🟡 권장 |

---

### 2.8 TripParticipant Domain

#### TripParticipantService 테스트 케이스

| 테스트 항목 | 입력 | 기대 출력 | 우선순위 |
|-----------|------|----------|---------|
| 동반자 추가 성공 | tripId, userName | TripParticipantRespDTO | 🟡 권장 |
| 동반자 목록 조회 | tripId | List<TripParticipantRespDTO> | 🟡 권장 |
| 동반자 삭제 성공 | tripId, userName | 삭제 완료 | 🟡 권장 |
| 존재하지 않는 사용자 추가 거절 | 없는 userName | Exception | 🟡 권장 |
| 이미 추가된 동반자 중복 거절 | 기존 참여자 | Exception | 🟡 권장 |
| 타인 여행에 동반자 추가 거절 | 다른 사용자의 tripId | Exception | 🟡 권장 |

---

## 3. 테스트 작성 순서 및 구현 현황

### Phase 1: 핵심 기능 (🔴 필수) - ✅ 완료

| 서비스 | 테스트 파일 | 테스트 수 | 상태 |
|--------|------------|----------|------|
| UserService | UserServiceTest.java | 21개 | ✅ |
| CommunityQueryService | CommunityQueryServiceTest.java | 5개 | ✅ |
| CommunityUpdateService | CommunityUpdateServiceTest.java | 7개 | ✅ |
| CommentQueryService | CommentQueryServiceTest.java | 5개 | ✅ |
| CommentUpdateService | CommentUpdateServiceTest.java | 8개 | ✅ |
| TripService | TripServiceTest.java | 26개 | ✅ |
| ItineraryQueryService | ItineraryQueryServiceTest.java | 6개 | ✅ |
| ItineraryUpdateService | ItineraryUpdateServiceTest.java | 9개 | ✅ |

### Phase 2: 보조 기능 (🟡 권장) - ✅ 완료

| 서비스 | 테스트 파일 | 테스트 수 | 상태 |
|--------|------------|----------|------|
| CommunityLikeService | CommunityLikeServiceTest.java | 8개 | ✅ |
| CommunityReportService | CommunityReportServiceTest.java | 5개 | ✅ |
| CommentLikeService | CommentLikeServiceTest.java | 8개 | ✅ |
| CommentReportService | CommentReportServiceTest.java | 5개 | ✅ |
| ReservationQueryService | ReservationQueryServiceTest.java | 7개 | ✅ |
| ReservationUpdateService | ReservationUpdateServiceTest.java | 9개 | ✅ |

### Phase 3: 부가 기능 (🟢 선택) - 📋 미구현

| 서비스 | 상태 | 비고 |
|--------|------|------|
| PreferTripService | 📋 미구현 | 선호 여행지 관리 |
| TripParticipantService | 📋 미구현 | 동반자 관리 |
| BannerService | 📋 미구현 | 배너 관리 |
| NoticeService | 📋 미구현 | 공지사항 관리 |
| ManageUserService | 📋 미구현 | 관리자 기능 |

**총 테스트 수**: 166개 (전체 통과)

---

## 4. 테스트 코드 작성 가이드

### 4.1 기본 구조

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private UserService userService;
    
    @Nested
    @DisplayName("회원가입")
    class RegisterTest {
        
        @Test
        @DisplayName("[성공] 정상 입력으로 회원가입")
        void registerUser_WithValidInput_ShouldCreateUser() {
            // given - 테스트 준비
            UserRegisterDTO dto = createValidDTO();
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUserName(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            
            // when - 테스트 실행
            userService.registerUser(dto);
            
            // then - 결과 검증
            verify(userRepository).save(any(User.class));
            verify(emailService).sendVerificationEmail(anyString(), anyString());
        }
        
        @Test
        @DisplayName("[실패] 중복 이메일로 회원가입 시 예외 발생")
        void registerUser_WithDuplicateEmail_ShouldThrowException() {
            // given
            UserRegisterDTO dto = createValidDTO();
            when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
            
            // when & then
            assertThatThrownBy(() -> userService.registerUser(dto))
                .isInstanceOf(BadRequestExceptionMessage.class);
        }
    }
    
    private UserRegisterDTO createValidDTO() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123!");
        dto.setUserName("testUser");
        dto.setFirstName("Test");
        dto.setLastName("User");
        return dto;
    }
}
```

### 4.2 테스트 데이터 팩토리 패턴

테스트 데이터는 `fixture` 패키지에서 관리한다:

```java
// src/test/java/com/jandi/plan_backend/fixture/UserFixture.java

public class UserFixture {
    
    public static User createNormalUser() {
        User user = new User();
        user.setUserId(1);
        user.setEmail("user@example.com");
        user.setUserName("normalUser");
        user.setVerified(true);
        user.setReported(false);
        user.setRole(0); // 일반 사용자
        return user;
    }
    
    public static User createAdminUser() {
        User user = createNormalUser();
        user.setUserId(2);
        user.setRole(2); // 관리자
        return user;
    }
    
    public static User createRestrictedUser() {
        User user = createNormalUser();
        user.setReported(true);
        return user;
    }
}
```

### 4.3 블랙박스 테스트 체크리스트

각 메서드에 대해 다음을 확인한다:

```
□ 정상 입력 → 정상 출력
□ 경계값 입력 (빈 문자열, 0, MAX값)
□ null 입력 처리
□ 권한 없는 접근 거절
□ 존재하지 않는 리소스 접근
□ 중복 요청 처리 (이미 좋아요한 상태 등)
□ 트랜잭션 처리 확인
□ 예외 타입 검증
```

---

## 5. 테스트 디렉토리 구조 (현재)

```
src/test/java/com/jandi/plan_backend/
├── PlanBackendApplicationTests.java
├── fixture/                          # 테스트 데이터 팩토리
│   ├── UserFixture.java             # 사용자 테스트 데이터
│   ├── CommunityFixture.java        # 게시물 테스트 데이터
│   ├── CommentFixture.java          # 댓글 테스트 데이터
│   ├── TripFixture.java             # 여행 테스트 데이터
│   ├── ItineraryFixture.java        # 일정 테스트 데이터
│   └── ReservationFixture.java      # 예약 테스트 데이터
├── user/
│   └── service/
│       └── UserServiceTest.java     # 회원가입, 로그인, 인증 등 21개 테스트
├── commu/
│   ├── community/
│   │   └── service/
│   │       ├── CommunityQueryServiceTest.java   # 게시물 조회 5개 테스트
│   │       ├── CommunityUpdateServiceTest.java  # 게시물 CRUD 7개 테스트
│   │       ├── CommunityLikeServiceTest.java    # 좋아요 8개 테스트
│   │       └── CommunityReportServiceTest.java  # 신고 5개 테스트
│   └── comment/
│       └── service/
│           ├── CommentQueryServiceTest.java     # 댓글 조회 5개 테스트
│           ├── CommentUpdateServiceTest.java    # 댓글 CRUD 8개 테스트
│           ├── CommentLikeServiceTest.java      # 좋아요 8개 테스트
│           └── CommentReportServiceTest.java    # 신고 5개 테스트
└── tripPlan/
    ├── trip/
    │   └── service/
    │       └── TripServiceTest.java             # 여행 CRUD 26개 테스트
    ├── itinerary/
    │   └── service/
    │       ├── ItineraryQueryServiceTest.java   # 일정 조회 6개 테스트
    │       └── ItineraryUpdateServiceTest.java  # 일정 CRUD 9개 테스트
    └── reservation/
        └── service/
            ├── ReservationQueryServiceTest.java   # 예약 조회 7개 테스트
            └── ReservationUpdateServiceTest.java  # 예약 CRUD 9개 테스트
```

---

## 6. 테스트 실행 명령

```bash
# 전체 테스트 실행
./gradlew test

# 특정 클래스 테스트
./gradlew test --tests "UserServiceTest"

# 특정 패키지 테스트
./gradlew test --tests "com.jandi.plan_backend.user.service.*"

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

---

## 7. 테스트 커버리지 목표

| 레이어 | 목표 | 비고 |
|--------|------|------|
| Service | 80% 이상 | 핵심 비즈니스 로직 |
| Repository | 통합 테스트 | 별도 계획 |
| Controller | 선택적 | MockMvc 고려 |

---

## 8. 주의사항

### 트랜잭션 동기화 매니저

단위 테스트에서 `@Transactional`이 붙은 서비스를 테스트할 때, `TransactionSynchronizationManager`를 수동으로 초기화해야 한다:

```java
@BeforeEach
void setUp() {
    TransactionSynchronizationManager.initSynchronization();
    // ... 테스트 데이터 설정
}

@AfterEach
void tearDown() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.clearSynchronization();
    }
}
```

### N+1 문제 테스트

페이징 쿼리는 반드시 @EntityGraph와 Batch Fetch Size를 고려하여 테스트한다. [1-Batch-Fetch-Size-설정.md](../trouble_shooting/1-Batch-Fetch-Size-설정.md) 참고.

### 트랜잭션 범위

LAZY 로딩된 엔티티는 @Transactional 범위 내에서만 접근 가능하다. [2-Entity-FetchType-LAZY-명시.md](../trouble_shooting/2-Entity-FetchType-LAZY-명시.md) 참고.

### ReflectionTestUtils 사용

final 필드를 가진 DTO를 테스트할 때는 `ReflectionTestUtils`를 사용한다:

```java
ReportReqDTO reportReqDTO = new ReportReqDTO();
ReflectionTestUtils.setField(reportReqDTO, "contents", "부적절한 내용");
```

---

## 9. 테스트 품질 검토 결과 (2026-01-02)

### ✅ 장점

1. **블랙박스 테스트 원칙 준수**: 모든 테스트가 입력→출력 기반으로 작성됨
2. **동등분할 테스트 적용**: 유효 입력 / 무효 입력으로 명확히 구분
3. **Fixture 패턴 활용**: 테스트 데이터의 재사용성과 일관성 확보
4. **@Nested 클래스 구조**: 기능별로 테스트 그룹화하여 가독성 향상
5. **명확한 테스트 명명**: `[성공]`/`[실패]` 접두사로 의도 명확히 표현
6. **예외 검증**: 예외 타입과 메시지 모두 검증

### 📋 향후 개선 필요 사항

1. **경계값 테스트 추가**: 페이지네이션의 경계값 (page=0, size=0 등) 테스트 추가 권장
2. **Phase 3 테스트 구현**: 선택적 기능에 대한 테스트 작성 고려
3. **통합 테스트 계획**: Repository 레이어 테스트 (@DataJpaTest) 추가 검토

---

## 10. 다음 단계

1. ~~Fixture 클래스 생성 (UserFixture, CommunityFixture 등)~~ ✅ 완료
2. ~~Phase 1 테스트 작성 시작 (UserService 부터)~~ ✅ 완료
3. ~~Phase 2 테스트 작성 (좋아요/신고/예약 서비스)~~ ✅ 완료
4. Phase 3 테스트 작성 (선택적)
5. CI/CD에 테스트 실행 단계 추가
6. JaCoCo로 커버리지 측정

