# Repository EntityGraph 추가

## 문제

LAZY 로딩과 Batch Fetch Size 설정만으로는 모든 N+1 문제를 해결할 수 없다. 특히 페이징 쿼리에서 연관 엔티티에 접근하면 여전히 추가 쿼리가 발생한다.

### 문제 시나리오

게시글 목록 조회 후 각 게시글의 작성자 이름 표시:
```java
Page<Community> posts = communityRepository.findAll(pageable);
posts.forEach(post -> post.getUser().getUserName()); // 추가 쿼리 발생
```

Batch Fetch Size가 100이면 2번의 쿼리로 줄어들지만, 1번의 쿼리로 완전히 해결할 수 있다.

## 해결 방법

### 1. @EntityGraph
Spring Data JPA가 제공하는 어노테이션으로, 쿼리 실행 시 함께 조회할 연관 엔티티를 지정한다.

```java
@EntityGraph(attributePaths = {"user"})
Page<Community> findAll(Pageable pageable);
```

### 2. JOIN FETCH (JPQL)
JPQL에서 직접 JOIN FETCH 절을 사용한다.

```java
@Query("SELECT c FROM Community c JOIN FETCH c.user")
List<Community> findAllWithUser();
```

### 선택 기준

| 방법 | 장점 | 단점 |
|------|------|------|
| @EntityGraph | 간단, Spring Data 메서드 네이밍 규칙과 함께 사용 가능 | 복잡한 조건 표현 어려움 |
| JOIN FETCH | 유연한 조건 표현, 복잡한 쿼리 가능 | JPQL 직접 작성 필요 |

## 적용 내용

### CommunityRepository

```java
@EntityGraph(attributePaths = {"user"})
Page<Community> findAll(Pageable pageable);

@EntityGraph(attributePaths = {"user"})
Optional<Community> findByPostId(Integer postId);
```

### TripRepository

```java
@EntityGraph(attributePaths = {"user", "city", "city.country"})
Page<Trip> findAll(Pageable pageable);

@EntityGraph(attributePaths = {"user", "city", "city.country"})
Optional<Trip> findByTripId(Integer tripId);

@EntityGraph(attributePaths = {"user", "city", "city.country"})
Page<Trip> findByUser(User user, Pageable pageable);

@EntityGraph(attributePaths = {"user", "city", "city.country"})
Page<Trip> findByPrivatePlan(Boolean privatePlan, Pageable pageable);

@EntityGraph(attributePaths = {"user", "city", "city.country"})
List<Trip> findTop10ByPrivatePlanFalseOrderByLikeCountDesc();

@EntityGraph(attributePaths = {"user", "city", "city.country"})
Page<Trip> findByPrivatePlanOrUser(boolean b, User user, Pageable pageable);
```

복잡한 조건의 쿼리는 JOIN FETCH 사용:
```java
@Query("SELECT DISTINCT t FROM Trip t " +
       "LEFT JOIN FETCH t.user " +
       "LEFT JOIN FETCH t.city c " +
       "LEFT JOIN FETCH c.country " +
       "WHERE t.privatePlan = false OR t.user = :user OR t.tripId IN " +
       "(SELECT tp.trip.tripId FROM TripParticipant tp WHERE tp.participant = :user)")
Page<Trip> findVisibleTrips(User user, Pageable pageable);
```

### CommentRepository

```java
@EntityGraph(attributePaths = {"community"})
Page<Comment> findByCommunityPostIdAndParentCommentIsNull(Integer postId, Pageable pageable);
```

## 생성되는 쿼리 비교

### EntityGraph 적용 전
```sql
-- 1. 게시글 조회
SELECT * FROM community LIMIT 10;
-- 2~11. 각 게시글의 User 조회 (N번)
SELECT * FROM user WHERE user_id = 1;
SELECT * FROM user WHERE user_id = 2;
...
```

### EntityGraph 적용 후
```sql
-- 1. 게시글 + User 한 번에 조회
SELECT c.*, u.* 
FROM community c 
LEFT JOIN user u ON c.user_id = u.user_id 
LIMIT 10;
```

### Trip 조회 (중첩 연관관계)
```sql
-- EntityGraph 적용 후
SELECT t.*, u.*, c.*, co.* 
FROM trip t 
LEFT JOIN user u ON t.user_id = u.user_id 
LEFT JOIN city c ON t.city_id = c.city_id 
LEFT JOIN country co ON c.country_id = co.country_id 
WHERE t.private_plan = false;
```

## 개선 효과

| 시나리오 | 적용 전 | 적용 후 |
|----------|---------|---------|
| 게시글 10개 + User | 11회 | 1회 |
| 여행 10개 + User + City + Country | 31회 | 1회 |
| 댓글 10개 + Community | 11회 | 1회 |

## 주의사항

### 1. 페이징과 JOIN FETCH

컬렉션(OneToMany)에 JOIN FETCH를 사용하면 페이징이 메모리에서 수행되어 성능 저하가 발생할 수 있다.

```java
// 위험: tripLikes는 컬렉션
@EntityGraph(attributePaths = {"user", "tripLikes"})
Page<Trip> findAll(Pageable pageable);
// HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory
```

**해결책**: 컬렉션은 Batch Fetch Size로 처리하고, 단일 연관관계만 EntityGraph 사용.

### 2. 카테시안 곱 문제

여러 컬렉션을 동시에 JOIN FETCH하면 카테시안 곱이 발생한다.

```java
// 위험: 두 개의 컬렉션 동시 fetch
@EntityGraph(attributePaths = {"comments", "likes"})
// MultipleBagFetchException 발생 가능
```

**해결책**: 
- 컬렉션은 하나만 JOIN FETCH
- 나머지는 Batch Fetch Size로 처리
- 또는 별도 쿼리로 분리

### 3. DTO Projection과 함께 사용

Entity 전체가 필요 없다면 DTO Projection이 더 효율적일 수 있다.

```java
@Query("SELECT new com.example.dto.CommunityDTO(c.postId, c.title, u.userName) " +
       "FROM Community c JOIN c.user u")
List<CommunityDTO> findAllDTO();
```
