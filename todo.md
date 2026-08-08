# 물리 FK 조사 결과 및 개선 TODO

**조사일**: 2026-08-08
**대상**: `jandi_plan_backend` 전체 엔티티, 삭제 로직, Git 이력
**비고**: 두 AI 에이전트(Claude / Codex)의 독립 조사 결과를 교차 검증하여 통합한 문서

---

## 1. 결론

| 항목 | 결과 | 확정도 |
|:---|:---|:---|
| 물리 FK 존재 여부 | **존재. 총 29개** | **확정** (Git 이력으로 입증) |
| `ConstraintMode.NO_CONSTRAINT` 사용 | 0건 | 확정 |
| 대상 엔티티 | 17개 | 확정 |
| 실제 장애 | **회원 탈퇴 API 실패 2건** | 확정 |
| 운영 DB의 현재 `ddl-auto` 값 | 미확인 | **저장소만으로 확인 불가** |
| 근본 원인 | 물리 FK 자체가 아니라, **FK를 전부 커버하지 못하는 수동 cascade 삭제 로직** | — |

> 두 조사 모두 FK 29개·`NO_CONSTRAINT` 0건이라는 수치는 동일하게 도출했다.
> 차이는 **"실 DB에 물리 FK가 실제로 존재하는가"** 였고, 이는 아래 §2-2의 Git 이력으로 확정되었다.

---

## 2. 근거

### 2-1. 스키마 생성 이력

- `.github/copilot-instructions.md:106` — `spring.jpa.hibernate.ddl-auto=update` (과거 운영 설정)
- `docs/Improvement_Roadmap.md:62` — P3 항목에서 운영 DB가 `update`임을 스스로 지적
- `src/main/resources/application.properties.example:20` — 현재 `validate`

`ddl-auto=update` 시점에 Hibernate가 `@JoinColumn` 29개에 대해 물리 FK를 생성했고,
`validate`로 전환해도 **기존 제약은 삭제되지 않으며**, `validate`는 FK 존재 여부를 검증하지도 않는다.

### 2-2. Git 이력 — 물리 FK 존재의 결정적 증거

> **런타임에 "외래 키 제약 조건 오류"가 발생했다는 것은, 실 DB에 물리 FK가 존재한다는 직접 증거다.**
> 소스 코드만으로는 확정할 수 없다는 한계가 여기서 해소된다.

| 커밋 | 날짜 | 내용 |
|:---|:---|:---|
| `27204cf` | 2025-02-23 | Trip 삭제 시 **외래키 종속성 때문에 삭제 불가** 문제 수정 |
| `999616f` | 2025-03-03 | 외래 키 제약 조건을 **이름으로 명시 지정**하여 중복 오류 회피 |
| `d7b7ba6` | 2025-03-04 | 외래키 문제 수정 (`PreferTripService`, `TripRepository`) |
| `5dc5b9f` | 2025-03-04 | 여행계획 삭제 시 예약·일정 자동 삭제로 **FK 제약 오류 회피** |
| `0a511ec` | 2025-03-09 | **댓글 삭제 시 외래키 오류** 해결 |

약 3주에 걸쳐 FK 위반 장애를 5회 수정한 이력이 있다. 모두 애플리케이션 레벨 우회로 대응했고,
제약 자체를 정리한 커밋은 없다.

### 2-3. 명시적 FK 선언

`999616f`의 결과물이 현재 코드에 남아 있다:

```java
// commu/community/entity/CommunityReported.java:18-24
@JoinColumn(name = "post_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_REPORTED_COMMUNITY"))
@JoinColumn(name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "FK_REPORTED_USER"))
```

### 2-4. 남은 불확실성 — 운영 설정은 저장소 밖에 있음

```dockerfile
# Dockerfile:51
ENTRYPOINT ["java", "-Dspring.config.location=file:/app/config/application.properties", "-jar", "app.jar"]
```

운영 설정은 home-server 저장소의 `docker-compose.apps.yml`이 마운트하는 `/app/config/application.properties`에서 주입되며,
`src/main/resources/application.properties`는 `.gitignore` 대상이다.
**따라서 운영 환경의 실제 `ddl-auto` 값은 이 저장소만으로 확인할 수 없다.** (→ P1 TODO)

### 2-5. 실 DB 확인용 쿼리

> 작업 착수 전 **로컬·개발·운영 각 환경에서** 실행하여 FK 목록을 확정하고 결과를 보관할 것.

```sql
SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE CONSTRAINT_SCHEMA = DATABASE()
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, CONSTRAINT_NAME, ORDINAL_POSITION;
```

---

## 3. 물리 FK 전체 목록 (29개)

### 3-1. 여행 및 일정 — 8개

| 테이블 | 컬럼 | 참조 대상 | 선언 위치 |
|:---|:---|:---|:---|
| `trip` | `user_id` | `users` | `Trip.java:32` |
| `trip` | `city_id` | `city` | `Trip.java:60` |
| `trip_like` | `trip_id` | `trip` | `TripLike.java:20` |
| `trip_like` | `user_id` | `users` | `TripLike.java:25` |
| `trip_participant` | `trip_id` | `trip` | `TripParticipant.java:23` |
| `trip_participant` | `participant_user_id` | `users` | `TripParticipant.java:28` |
| `itinerary` | `trip_id` | `trip` | `Itinerary.java:20` |
| `reservation` | `trip_id` | `trip` | `Reservation.java:17` |

### 3-2. 커뮤니티 및 댓글 — 11개

| 테이블 | 컬럼 | 참조 대상 | 선언 위치 |
|:---|:---|:---|:---|
| `community` | `user_id` | `users` | `Community.java:20` |
| `community_reported` | `post_id` | `community` | `CommunityReported.java:18` |
| `community_reported` | `user_id` | `users` | `CommunityReported.java:23` |
| `community_like` | `community_id` | `community` | `CommunityLike.java:16` |
| `community_like` | `user_id` | `users` | `CommunityLike.java:21` |
| `comments` | `post_id` | `community` | `Comment.java:18` |
| `comments` | `parent_comment_id` | `comments` (자기참조) | `Comment.java:22` |
| `comment_like` | `comment_id` | `comments` | `CommentLike.java:16` |
| `comment_like` | `user_id` | `users` | `CommentLike.java:21` |
| `comment_reported` | `comment_id` | `comments` | `CommentReported.java:18` |
| `comment_reported` | `user_id` | `users` | `CommentReported.java:22` |

### 3-3. 사용자 및 지역 — 10개

| 테이블 | 컬럼 | 참조 대상 | 선언 위치 |
|:---|:---|:---|:---|
| `city` | `country_id` | `country` | `City.java:20` |
| `city` | `continent_id` | `continent` | `City.java:24` |
| `country` | `continent_id` | `continent` | `Country.java:21` |
| `role_log` | `target_user_id` | `users` | `RoleLog.java:21` |
| `user_city_preference` | `user_id` | `users` | `UserCityPreference.java:16` |
| `user_city_preference` | `city_id` | `city` | `UserCityPreference.java:21` |
| `user_country_preference` | `user_id` | `users` | `UserCountryPreference.java:16` |
| `user_country_preference` | `country_id` | `country` | `UserCountryPreference.java:21` |
| `user_continent_preference` | `user_id` | `users` | `UserContinentPreference.java:16` |
| `user_continent_preference` | `continent_id` | `continent` | `UserContinentPreference.java:21` |

---

## 4. 논리 참조 — 정책이 혼재되어 있음

프로젝트 전체가 물리 FK로 통일된 것은 아니다. 다음은 연관관계 없이 식별자 값만 저장한다.

| 필드 | 참조 대상 | 위치 |
|:---|:---|:---|
| `Comment.userId` (`Integer`) | `users` | `Comment.java:26` |
| `Itinerary.placeId` (`Long`) | `place` | `Itinerary.java:24` |
| `Image.targetType` + `Image.targetId` | 다형적 참조 (프로필·게시글·공지·여행) | `Image.java:18-21` |

`Comment`는 **같은 엔티티 안에서** `community`는 물리 FK, `userId`는 논리 참조로 갈린다.
`Itinerary`도 `trip`은 물리 FK, `placeId`는 논리 참조다.
삭제·정합성 로직을 작성할 때마다 관계별로 개별 확인이 필요한 상태.

`Image`의 다형적 참조는 구조상 FK를 걸 수 없으므로, 정합성은 애플리케이션이 책임진다.

---

## 5. 물리 FK로 인한 코드 부채

### 5-1. 손으로 짠 cascade 삭제

`UserService.deleteUser()` (`user/service/UserService.java:272-320`) — **45줄짜리 수동 cascade**.
답글 → 댓글 → 게시글 → 신고 → 좋아요 → 여행 → 선호도 순으로 부모 삭제 전 자식을 일일이 제거.

### 5-2. cascade 설정이 있는데도 중복 수동 삭제

`TripService.deleteMyTrip()` (`tripPlan/trip/service/TripService.java:305-322`)은
`Trip` 엔티티에 이미 `cascade = CascadeType.ALL, orphanRemoval = true`가 걸려 있음에도
`itineraryRepository.deleteAll(...)`, `reservationRepository.deleteAll(...)`를 수동 호출한다.
(`5dc5b9f`와 `27204cf`의 대응이 중첩된 결과로 보임)

### 5-3. 주의: JPA cascade ≠ DB FK

`cascade`와 `orphanRemoval`은 **ORM 레벨 동작**이며, DB의 `ON DELETE CASCADE`와는 별개다.
Hibernate가 생성한 FK에는 `ON DELETE` 옵션이 붙지 않으므로, JPA를 거치지 않는 직접 SQL 삭제는 여전히 FK 위반으로 실패한다.

---

## 6. 확정된 버그 — 회원 탈퇴 실패

`UserService`는 클래스 레벨 `@Transactional` (`UserService.java:43`).
FK 위반 시 **전체 롤백 → 회원 탈퇴가 통째로 실패**한다.

`users`를 참조하는 FK 12개를 `deleteUser()`와 전수 대조한 결과:

| 테이블 | 컬럼 | 상태 |
|:---|:---|:---|
| `community` / `community_like` / `community_reported` | `user_id` | 처리됨 |
| `comment_like` / `comment_reported` | `user_id` | 처리됨 |
| `trip` / `trip_like` | `user_id` | 처리됨 |
| `user_city_preference` | `user_id` | 처리됨 |
| **`role_log`** | `target_user_id` | **누락 — 장애 발생** |
| **`trip_participant`** | `participant_user_id` | **누락 — 장애 발생** |
| `user_country_preference` | `user_id` | 누락 — 잠재 |
| `user_continent_preference` | `user_id` | 누락 — 잠재 |

### 6-1. `role_log` — 등급이 바뀐 적 있는 유저는 탈퇴 불가

`UserEntityListener.afterUpdate()` (`user/entity/UserEntityListener.java:30-43`)가 role 변경 시마다 자동 삽입.
`ManageUserService.changeUserRole()` (`ManageUserService.java:111-117`)에서도 직접 삽입.

→ **관리자가 승격시킨 유저, 제재당한 유저는 모두 탈퇴 API가 실패한다.**

### 6-2. `trip_participant` — 타인의 여행에 참여한 유저는 탈퇴 불가

본인 소유 여행은 `deleteMyTrip()`의 cascade로 참여자 행까지 정리되지만,
**해당 유저가 타인의 여행에 동반자로 등록된 행**은 어디서도 삭제되지 않는다.
(삽입 지점: `TripParticipantService.java:54-59`)

→ **친구 여행에 초대받은 적 있는 유저는 탈퇴 API가 실패한다.**

### 6-3. `user_country_preference` / `user_continent_preference` — 잠재 지뢰

엔티티와 테이블은 존재하나 저장하는 코드가 전무하다(Repository·서비스 사용처 없음).
현재는 빈 테이블이라 장애가 없으나, **해당 기능을 활성화하는 순간 탈퇴가 깨진다.**

### 6-4. `UserCleanupService` — 구조적 취약점 (현재는 안전)

`UserCleanupService.cleanupUnverifiedUsers()` (`user/service/UserCleanupService.java:30-40`)는
`deleteUser()`를 거치지 않고 `userRepository.delete(user)`를 직접 호출한다.

**현재는 안전하다.** 로그인이 `verified` 검사로 차단되고(`UserService.java:152-154`),
소셜 로그인 가입자는 `verified=true`로 생성되므로(`GoogleService.java:184`, `KakaoService.java:190`,
`NaverService.java:185`) 미인증 유저에게는 연관 행이 생기지 않는다.

다만 이는 **코드 어디에도 명시되지 않은 암묵적 전제**에 의존한다.
향후 미인증 상태에서 무언가를 기록하는 기능이 추가되면 FK 위반이 발생하며,
이 메서드는 루프 전체가 하나의 `@Transactional`(`UserCleanupService.java:29`)이므로
**유저 1명 때문에 배치 전체가 롤백된다.**

---

## 7. 판단 기준 — "물리 FK를 쓰면 안 된다"에 대하여

이 규칙은 절대 규칙이 아니라 **맥락이 있는 규칙**이다. 샤딩, 서비스별 DB 분리(MSA), 대규모 쓰기,
무중단 스키마 변경 요구가 있는 환경에서 도출된 것으로, 단일 애플리케이션·단일 MySQL을 쓰는
본 프로젝트 규모에서는 물리 FK가 오히려 참조 무결성을 보장하는 쪽에 가깝다.

**본 프로젝트의 진짜 문제는 "물리 FK가 있다"가 아니라 "물리 FK가 있는데 삭제 로직이 그것을 전부 커버하지 못한다"이다.**

FK를 먼저 제거하면 탈퇴는 성공하겠지만, 대신 `role_log`와 `trip_participant`에 고아 행이 남는다.
현재는 DB가 에러로 이를 막아주고 있으므로, **FK 제거를 선행하면 장애가 조용한 데이터 오염으로 바뀔 뿐이다.**

FK 제거 여부는 회사 규칙을 그대로 적용하기보다 다음을 먼저 결정한 뒤 판단할 것:

- DB를 여러 서비스가 공유하는가, 혹은 서비스별로 분리할 예정인가
- 샤딩이나 파티셔닝 계획이 있는가
- FK 검사 비용이 실제 병목으로 측정되었는가
- 애플리케이션이 참조 무결성과 삭제 순서를 책임질 수 있는가 (현재는 **불가** — §6이 그 증거)

---

## 8. TODO

### P1 — 버그 수정 및 현황 확정 (즉시)

> 현재 사용자가 실제로 겪고 있는 장애. FK 정책 논의와 무관하게 선행되어야 함.

- [ ] `information_schema` 쿼리를 **로컬·개발·운영** 각 환경에서 실행해 실제 FK 목록 확정 및 결과 보관 (§2-5)
- [ ] home-server 저장소의 `/app/config/application.properties`에서 운영 `ddl-auto` 실제 값 확인 (§2-4)
- [ ] `UserService.deleteUser()`에 `role_log` 삭제 추가
- [ ] `UserService.deleteUser()`에 `trip_participant` 삭제 추가 (참여자로 등록된 행)
- [ ] 회원 탈퇴 통합 테스트 작성
  - [ ] 등급 변경 이력이 있는 유저 탈퇴
  - [ ] 타인 여행에 동반자로 참여 중인 유저 탈퇴
  - [ ] 게시글·댓글·좋아요·신고·여행이 모두 있는 유저 탈퇴

### P2 — 마이그레이션 도구 도입

> 현재 Flyway·Liquibase·`schema.sql` 등 버전 관리되는 DDL이 전무하여,
> FK 정책을 안전하게 변경할 수단 자체가 없음. `docs/Improvement_Roadmap.md` P3와 연계.

- [ ] Flyway 또는 Liquibase 도입
- [ ] 현재 운영 스키마를 baseline으로 스냅샷
- [ ] 신규 DB를 `ddl-auto=validate`만으로 구성 가능하도록 초기 스키마 생성 절차 마련
- [ ] `ddl-auto`를 `validate` → `none`으로 전환

### P3 — FK 정책 결정 및 일관성 정리

- [ ] §7의 4개 질문에 답하여 **물리 FK 유지 / 논리 FK 전환** 중 택일하고 이유를 문서화
- [ ] 결정이 "논리 FK 전환"인 경우에만 아래 수행
  - [ ] 각 관계별 **고아 데이터 존재 여부 사전 검사**
  - [ ] FK가 암묵적으로 제공하던 **인덱스**를 명시적 인덱스로 대체 설계
  - [ ] 참조 무결성 검증을 애플리케이션 레이어로 이관하는 설계
  - [ ] 버전 관리되는 마이그레이션으로 제약 제거 (P2 선행 필수)
  - [ ] `@ForeignKey(ConstraintMode.NO_CONSTRAINT)` 적용으로 재생성 방지
- [ ] `Comment.userId`를 결정된 정책에 맞춰 통일 (§4)
- [ ] `Itinerary.placeId` 동일 처리
- [ ] 미사용 엔티티 정리 판단: `UserCountryPreference`, `UserContinentPreference`
      (기능 구현 예정이면 삭제 로직 먼저 포함, 아니면 엔티티·테이블 제거)

### P4 — cascade 로직 정리

- [ ] `TripService.deleteMyTrip()`의 중복 수동 삭제 제거 (§5-2)
- [ ] `TripService`에 클래스 레벨 `@Transactional` 부재 확인 (`TripService.java:35-36`).
      삭제가 여러 Repository 호출에 걸쳐 있어 원자성이 보장되지 않음
- [ ] 수동 cascade를 DB `ON DELETE CASCADE` 또는 JPA cascade 중 **하나로 일원화**
- [ ] `UserCleanupService`가 `deleteUser()`를 재사용하도록 변경 검토 (§6-4).
      현재는 안전하나 암묵적 전제에 의존하며, 배치 전체가 단일 트랜잭션이라 1건 실패 시 전량 롤백됨
- [ ] 동시성 상황에서 고아 데이터가 발생하지 않는지 테스트

---

## 부록 A. 두 조사 결과의 차이

| 항목 | Claude | Codex | 통합 결과 |
|:---|:---|:---|:---|
| FK 개수 | 29 | 29 | **일치** |
| `NO_CONSTRAINT` | 0 | 0 | **일치** |
| 논리 참조 식별 | 3건 | 3건 | **일치** |
| 실 DB 존재 확정 | 확정 주장 | "소스만으로 확정 불가" 유보 | **Git 이력으로 확정** (§2-2) |
| 운영 설정 주입 경로 | 미조사 | 지적함 | **Codex 지적 채택** (§2-4) |
| 구체적 버그 | 4건 식별 | 미조사 | **Claude 결과 채택** (§6) |
| FK 제거 시 인덱스 영향 | 미조사 | 지적함 | **Codex 지적 채택** (P3) |

Codex가 유보했던 "소스 코드만으로 실 DB의 물리 FK를 확정할 수 없다"는 타당한 지적이었으며,
Git 이력에서 런타임 FK 위반 수정 커밋 5건을 확인함으로써 해소되었다.

## 부록 B. 조사 방법

```bash
# @JoinColumn 총 개수
grep -rho "@JoinColumn" --include=*.java src/main | wc -l    # → 29

# FK 비활성화 선언 여부
grep -rn "NO_CONSTRAINT" --include=*.java src/main | wc -l   # → 0

# 런타임 FK 위반 수정 이력
git log --all --oneline -i --grep="외래\|FK\|constraint"
```
