# Entity FetchType.LAZY 명시

## 문제

JPA의 `@ManyToOne` 및 `@OneToOne` 관계는 기본 FetchType이 **EAGER**다. 이는 부모 엔티티를 조회할 때 연관된 엔티티를 자동으로 함께 조회한다는 의미다.

문제 발생 시나리오:
1. 게시글 목록 조회 시 각 게시글의 User 정보가 자동으로 조회됨
2. 여행 계획 조회 시 User, City, Country, Continent가 연쇄적으로 조회됨
3. 댓글 조회 시 Community, parentComment가 자동 조회됨

결과적으로 필요하지 않은 데이터까지 조회하여 성능 저하가 발생한다.

## 영향받는 엔티티

| 엔티티 | 연관관계 | 기본 FetchType | 문제 |
|--------|----------|---------------|------|
| Trip | User, City | EAGER | 여행 목록 조회 시 N+1 |
| Community | User | EAGER | 게시글 목록 조회 시 N+1 |
| Comment | Community, parentComment | EAGER | 댓글 조회 시 연쇄 N+1 |
| City | Country, Continent | EAGER | 도시 조회 시 연쇄 조회 |
| CommunityLike | Community, User | EAGER | 좋아요 목록 조회 시 N+1 |
| TripLike | Trip, User | EAGER | 좋아요 목록 조회 시 N+1 |
| TripParticipant | Trip, User | EAGER | 참가자 목록 조회 시 N+1 |
| Itinerary | Trip | EAGER | 일정 조회 시 N+1 |
| CommentLike | Comment, User | EAGER | 댓글 좋아요 조회 시 N+1 |
| CommunityReported | Community, User | EAGER | 신고 목록 조회 시 N+1 |
| CommentReported | Comment, User | EAGER | 댓글 신고 조회 시 N+1 |

## 해결 방법

모든 `@ManyToOne` 관계에 `fetch = FetchType.LAZY`를 명시적으로 추가한다.

### 변경 전
```java
@ManyToOne
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

### 변경 후
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

## 변경된 파일

| 파일 | 변경된 연관관계 |
|------|----------------|
| Trip.java | user, city |
| Community.java | user |
| Comment.java | community, parentComment |
| City.java | country, continent |
| CommunityLike.java | community, user |
| TripLike.java | trip, user |
| TripParticipant.java | trip, participant |
| Itinerary.java | trip |
| CommentLike.java | comment, user |
| CommunityReported.java | community, user |
| CommentReported.java | comment, user |

## 개선 효과

### EAGER 로딩 시 (변경 전)
```sql
-- 게시글 1개 조회
SELECT * FROM community WHERE post_id = 1;
-- User 자동 조회 (EAGER)
SELECT * FROM user WHERE user_id = ?;
```

### LAZY 로딩 시 (변경 후)
```sql
-- 게시글 1개 조회
SELECT * FROM community WHERE post_id = 1;
-- User는 실제로 접근할 때만 조회
```

### 쿼리 수 비교

| 시나리오 | EAGER | LAZY |
|----------|-------|------|
| 게시글 10개 조회 (User 미접근) | 11회 | 1회 |
| 여행 계획 10개 조회 (상세 미접근) | 31회+ | 1회 |
| 댓글 10개 조회 (Community 미접근) | 21회+ | 1회 |

## 주의사항

### 1. LazyInitializationException

LAZY 로딩된 엔티티에 트랜잭션 범위 밖에서 접근하면 예외가 발생한다.

```java
// 오류 발생 케이스
@Transactional
public Community getCommunity(Integer id) {
    return communityRepository.findById(id).orElseThrow();
}

// 트랜잭션 종료 후 User 접근 시 예외
Community c = service.getCommunity(1);
c.getUser().getUserName(); // LazyInitializationException!
```

### 2. 해결책

필요한 연관 엔티티는 트랜잭션 내에서 미리 로드하거나, `@EntityGraph`/`JOIN FETCH`를 사용한다.

```java
// 방법 1: 트랜잭션 내에서 접근
@Transactional
public CommunityDTO getCommunity(Integer id) {
    Community c = communityRepository.findById(id).orElseThrow();
    return new CommunityDTO(c, c.getUser()); // 트랜잭션 내에서 User 접근
}

// 방법 2: EntityGraph 사용
@EntityGraph(attributePaths = {"user"})
Optional<Community> findById(Integer id);
```

### 3. Batch Fetch Size와 함께 사용

`hibernate.default_batch_fetch_size=100` 설정과 함께 사용하면, LAZY 로딩 시에도 IN 절로 배치 조회되어 효율적이다.

```sql
-- 100개씩 배치 조회
SELECT * FROM user WHERE user_id IN (1, 2, 3, ..., 100);
```

## Batch Fetch Size와의 시너지

| 설정 | 게시글 10개 + User 조회 |
|------|------------------------|
| EAGER only | 11회 |
| LAZY only | 1회 (User 미접근 시) / 11회 (접근 시) |
| LAZY + Batch | 1회 (미접근) / 2회 (접근 시) |
