# Batch Fetch Size 설정

## 문제

JPA에서 `@ManyToOne`, `@OneToMany` 등의 연관관계가 있는 엔티티를 조회할 때, LAZY 로딩으로 설정되어 있더라도 연관 엔티티에 접근하면 각 엔티티마다 개별 쿼리가 실행된다. 이를 N+1 문제라고 한다.

예를 들어, 게시글 10개를 조회하고 각 게시글의 작성자(User) 정보에 접근하면:
- 1번: 게시글 10개 조회 (`SELECT * FROM community`)
- 10번: 각 게시글의 작성자 조회 (`SELECT * FROM user WHERE user_id = ?`)

총 11번의 쿼리가 실행된다.

## 해결 방법

### 1. @EntityGraph 또는 JOIN FETCH 사용
특정 쿼리에 대해 연관 엔티티를 함께 조회하도록 명시한다.

```java
@EntityGraph(attributePaths = {"user"})
Page<Community> findAll(Pageable pageable);
```

**장점**: 필요한 쿼리에만 적용 가능
**단점**: 모든 Repository 메서드에 개별 적용 필요

### 2. Hibernate Batch Fetch Size 설정
LAZY 로딩 시 연관 엔티티를 개별 조회하지 않고, IN 절을 사용하여 한 번에 여러 개를 조회하도록 한다.

```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=100
```

**장점**: 전역 설정으로 모든 LAZY 로딩에 자동 적용
**단점**: 모든 연관관계에 동일하게 적용되어 세밀한 제어 어려움

### 3. @BatchSize 어노테이션
특정 엔티티나 컬렉션에만 배치 페치 크기를 지정한다.

```java
@BatchSize(size = 100)
@OneToMany(mappedBy = "community")
private List<Comment> comments;
```

**장점**: 엔티티별 세밀한 제어 가능
**단점**: 모든 엔티티에 개별 적용 필요

## 적용한 방법

**Hibernate Batch Fetch Size 전역 설정**을 선택했다.

이유:
1. 프로젝트 전반에 N+1 문제가 광범위하게 존재하여 전역 설정이 효율적
2. 단일 설정으로 모든 LAZY 로딩에 적용되어 누락 위험 없음
3. 추후 특정 쿼리에 대해 @EntityGraph로 추가 최적화 가능

### 변경 내용

**파일**: `src/main/resources/application.properties`

```properties
# N+1 문제 해결을 위한 Batch Fetch Size 설정
# LAZY 로딩 시 연관 엔티티를 한 번에 100개씩 IN 절로 조회
spring.jpa.properties.hibernate.default_batch_fetch_size=100
```

## 개선 효과

### 쿼리 수 변화

| 시나리오 | 적용 전 | 적용 후 |
|----------|---------|---------|
| 게시글 10개 + User 조회 | 11회 | 2회 |
| 게시글 100개 + User 조회 | 101회 | 2회 |
| 게시글 150개 + User 조회 | 151회 | 3회 |

### 동작 원리

적용 전:
```sql
SELECT * FROM community;
SELECT * FROM user WHERE user_id = 1;
SELECT * FROM user WHERE user_id = 2;
-- ... 반복
```

적용 후:
```sql
SELECT * FROM community;
SELECT * FROM user WHERE user_id IN (1, 2, 3, ..., 100);
```

## 주의사항

1. **배치 크기 선정**: 너무 크면 IN 절의 파라미터가 많아져 쿼리 파싱 비용 증가. 일반적으로 100~1000 사이 권장.

2. **메모리 사용량**: 배치로 조회한 엔티티가 영속성 컨텍스트에 모두 로드되므로, 대용량 데이터 처리 시 주의 필요.

3. **추가 최적화 필요**: 이 설정만으로 완전한 해결이 아님. 중요한 쿼리에는 @EntityGraph나 JOIN FETCH로 추가 최적화 권장.
