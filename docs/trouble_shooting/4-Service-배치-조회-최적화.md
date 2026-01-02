# Service 배치 조회 최적화

## 문제점

### 기존 코드의 N+1 문제
CommentQueryService에서 댓글/답글 목록을 조회할 때, **개별 댓글마다** 다음 쿼리가 발생:

```java
// 기존 방식 - 댓글 N개당 2N개의 추가 쿼리 발생
comment -> {
    User commentAuthor = commentUtil.getCommentUser(comment);  // N번의 User 조회
    boolean liked = commentUtil.isLiked(comment, currentUser);  // N번의 Like 조회
    return new ParentCommentDTO(comment, commentAuthor, imageService, liked);
}
```

**10개의 댓글 조회 시:**
- 1번: 댓글 목록 조회
- 10번: 각 댓글의 작성자 조회 (N+1)
- 10번: 각 댓글의 좋아요 여부 조회 (N+1)
- **총 21개의 쿼리 실행**

## 해결 방법

### 1. Repository 배치 조회 메서드 추가

**UserRepository.java:**
```java
List<User> findAllByUserIdIn(List<Integer> userIds);
```

**CommentLikeRepository.java:**
```java
@Query("SELECT cl.comment.commentId FROM CommentLike cl " +
       "WHERE cl.user = :user AND cl.comment.commentId IN :commentIds")
Set<Integer> findLikedCommentIdsByUserAndCommentIds(
    @Param("user") User user, 
    @Param("commentIds") List<Integer> commentIds
);
```

### 2. CommentUtil 배치 메서드 추가

```java
/** 댓글 목록의 작성자 정보를 한 번에 조회 (배치) */
public Map<Integer, User> getCommentUsersMap(List<Comment> comments) {
    List<Integer> userIds = comments.stream()
            .map(Comment::getUserId)
            .distinct()
            .collect(Collectors.toList());
    
    if (userIds.isEmpty()) {
        return Collections.emptyMap();
    }
    
    List<User> users = userRepository.findAllByUserIdIn(userIds);
    return users.stream().collect(
        Collectors.toMap(User::getUserId, Function.identity())
    );
}

/** 사용자가 좋아요한 댓글 ID 목록을 한 번에 조회 (배치) */
public Set<Integer> getLikedCommentIds(List<Comment> comments, User user) {
    if (user == null || comments.isEmpty()) {
        return Collections.emptySet();
    }
    
    List<Integer> commentIds = comments.stream()
            .map(Comment::getCommentId)
            .collect(Collectors.toList());
    
    return commentLikeRepository.findLikedCommentIdsByUserAndCommentIds(
        user, commentIds
    );
}
```

### 3. PaginationService 배치 지원 메서드 추가

```java
/** 배치 매핑을 지원하는 페이지네이션 */
public static <T, R> Page<R> getPagedDataBatch(
        int page, int size, long totalCount,
        Function<Pageable, Page<T>> fetchFunction,
        BiFunction<List<T>, Pageable, List<R>> batchMapper) {
    // ...
    Page<T> entityPage = fetchFunction.apply(pageable);
    List<R> mappedContent = batchMapper.apply(entityPage.getContent(), pageable);
    return new PageImpl<>(mappedContent, pageable, totalCount);
}
```

### 4. CommentQueryService 배치 조회 적용

```java
@Transactional(readOnly = true)
public Page<ParentCommentDTO> getAllComments(Integer postId, int page, int size, String userEmail) {
    // ...
    return PaginationService.getPagedDataBatch(page, size, totalCount,
            pageable -> commentRepository.findByCommunityPostIdAndParentCommentIsNull(postId, pageable),
            (comments, pageable) -> {
                // 배치 조회: User 맵, 좋아요 Set을 한 번에 조회
                Map<Integer, User> userMap = commentUtil.getCommentUsersMap(comments);
                Set<Integer> likedIds = commentUtil.getLikedCommentIds(comments, currentUser);
                
                return comments.stream()
                        .map(comment -> {
                            User author = userMap.get(comment.getUserId());
                            boolean liked = likedIds.contains(comment.getCommentId());
                            return new ParentCommentDTO(comment, author, imageService, liked);
                        })
                        .collect(Collectors.toList());
            });
}
```

## 성능 개선 효과

### 쿼리 수 비교 (댓글 10개 기준)

| 항목 | 수정 전 | 수정 후 | 감소율 |
|------|---------|---------|--------|
| 댓글 목록 | 1 | 1 | - |
| User 조회 | 10 | 1 | 90% |
| Like 조회 | 10 | 1 | 90% |
| **총 쿼리 수** | **21** | **3** | **85.7%** |

### 시간 복잡도 개선
- **수정 전**: O(N) 쿼리 (댓글 수에 비례)
- **수정 후**: O(1) 쿼리 (상수)

## 수정된 파일 목록

### Repository 계층
| 파일 | 변경 내용 |
|------|-----------|
| [UserRepository.java](../src/main/java/com/jandi/plan_backend/user/repository/UserRepository.java) | `findAllByUserIdIn` 배치 조회 메서드 추가 |
| [CommentLikeRepository.java](../src/main/java/com/jandi/plan_backend/commu/comment/repository/CommentLikeRepository.java) | `findLikedCommentIdsByUserAndCommentIds` 배치 조회 메서드 추가 |
| [ImageRepository.java](../src/main/java/com/jandi/plan_backend/image/repository/ImageRepository.java) | `findAllByTargetTypeAndTargetIdIn` 배치 조회 메서드 추가 |

### Util 계층
| 파일 | 변경 내용 |
|------|-----------|
| [CommentUtil.java](../src/main/java/com/jandi/plan_backend/util/CommentUtil.java) | `getCommentUsersMap`, `getLikedCommentIds` 배치 메서드 추가 |
| [PaginationService.java](../src/main/java/com/jandi/plan_backend/util/service/PaginationService.java) | `getPagedDataBatch` BiFunction 지원 메서드 추가 |

### Service 계층
| 파일 | 변경 내용 |
|------|-----------|
| [CommentQueryService.java](../src/main/java/com/jandi/plan_backend/commu/comment/service/CommentQueryService.java) | `getAllComments`, `getAllReplies` 배치 조회 적용 |

## 적용 패턴

이 최적화 패턴은 다른 서비스에도 동일하게 적용 가능합니다:

1. **IN 절을 사용한 배치 Repository 메서드** 정의
2. **Util 클래스에 배치 조회 헬퍼 메서드** 추가
3. **Service에서 PaginationService.getPagedDataBatch** 사용
4. **BiFunction으로 전체 목록에 대한 배치 처리** 적용

## 주의사항

- 배치 조회 시 IN 절의 크기가 너무 크면 성능 저하 가능 (MySQL 기본 최대 1000개)
- Page size를 적절히 제한하여 사용 (현재 설정: 최대 100개)
